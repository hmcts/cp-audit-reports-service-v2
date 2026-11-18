package uk.gov.hmcts.cp.services;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.exception.AzureException;
import com.azure.core.util.IterableStream;
import com.azure.data.tables.TableClient;
import com.azure.data.tables.implementation.EntityPaged;
import com.azure.data.tables.models.TableEntity;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.hmcts.cp.entities.output.Report;
import uk.gov.hmcts.cp.mappers.ReportMapperImpl;
import uk.gov.hmcts.cp.properties.AzureProperties;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuditReportsServiceTest {
    private static final String ENDPOINT = "https://audit.blob.core.windows.net";
    private static final String CIRCULATION = "/generatedreports/report/generated/";

    @Mock
    TableClient tableClient;

    @Mock
    EntityPaged<TableEntity> tableEntities;

    @Mock
    AzureProperties azure;

    @Mock
    BlobServiceClient blobServiceClient;

    @Mock
    BlobContainerClient blobContainerClient;

    @Mock
    BlobClient blobClient;

    @Mock
    UserDelegationKey delegationKey;

    ObjectMapper mapper = JsonMapper.builder().configure(
            DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false
    ).build();

    AuditReportsService underTest;

    @Test
    void test_getReports() {

        underTest = new AuditReportsService(null, tableClient, mapper, null, null, null, null, null);

        // Given
        when(tableClient.listEntities()).thenReturn(new PagedIterable<>(() -> tableEntities));
        when(tableEntities.getElements()).thenReturn(new IterableStream<>(List.of(
                new TableEntity("partitionKey", "rowKey").
                        setProperties(Map.of(
                                "userEmail", "no@where.com",
                                "pipelineJobId", "12345"
                        ))
        )));

        // When
        var result = underTest.getReports();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNotNull(result.getFirst());

        assertEquals("12345", result.getFirst().pipelineJobId());
        assertEquals("no@where.com", result.getFirst().userEmail());
    }

    @Test
    void test_getReports_signsDownloadUrl() {
        final String name = "report/generated/report_pseudonymised.xlsx";
        final String url = ENDPOINT + "/generatedreports/" + name;
        when(azure.pseudonymisedDownloadsOnly()).thenReturn(true);
        when(azure.downloadUrlMinutesValid()).thenReturn(10);
        when(blobServiceClient.getBlobContainerClient("generatedreports")).thenReturn(blobContainerClient);
        when(blobContainerClient.getBlobClient(name)).thenReturn(blobClient);
        when(blobServiceClient.getUserDelegationKey(any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(delegationKey);
        when(blobClient.generateUserDelegationSas(any(BlobServiceSasSignatureValues.class), eq(delegationKey)))
                .thenReturn("synthetic-test-signature");

        assertEquals(url + "?synthetic-test-signature", reportsForUrl(url).getFirst().downloadUrl());
    }

    @Test
    void test_getReports_hidesUrlWhenSigningFails() {
        final String url = ENDPOINT + CIRCULATION + "report.csv";
        when(blobServiceClient.getBlobContainerClient("generatedreports"))
                .thenThrow(new IllegalStateException("Synthetic storage failure"));

        assertNull(reportsForUrl(url).getFirst().downloadUrl());
    }

    @Test
    void test_getReports_hidesUrlWhenStorageFails() {
        final String url = ENDPOINT + CIRCULATION + "report.xlsx";
        when(blobServiceClient.getBlobContainerClient("generatedreports"))
                .thenThrow(new AzureException("Synthetic Azure failure"));

        assertNull(reportsForUrl(url).getFirst().downloadUrl());
    }

    @Test
    void test_getReports_hidesMalformedUrl() {
        assertNull(reportsForUrl("invalid-url").getFirst().downloadUrl());
        verifyNoInteractions(blobServiceClient);
    }

    @Test
    void test_getReports_neverSignsFailedRowsOrExposesDiagnostics() {
        final List<Report> reports = reportsFor(List.of(row("20261118-oldRow", Map.of(
                "pipelineStatus", "FAILED", "downloadUrl", ENDPOINT + CIRCULATION + "stale_pseudonymised.xlsx",
                "pipelineStatusDescription", "internal stack trace", "hearingId", "display-reference"))));
        assertEquals("FAILED", reports.getFirst().pipelineStatus());
        assertNull(reports.getFirst().downloadUrl());
        assertNull(reports.getFirst().pipelineStatusDescription());
        assertNull(reports.getFirst().hearingId());
        verifyNoInteractions(blobServiceClient);
    }

    @Test
    void test_getReports_ordersBySubmissionEvenBeforePipelineStarts() {
        final List<Report> reports = reportsFor(List.of(
                row("20261118-legacy", Map.of("pipelineStartTime", "2026-11-18T10:15:00Z")),
                row("20261118-tieB", Map.of("submittedAt", "2026-11-18T10:30:00Z",
                        "pipelineStatus", "PENDING", "pipelineEndTime", "")),
                row("20261118-date", Map.of()),
                row("20261118-older", Map.of("submittedAt", "2026-11-18T10:00:00Z",
                        "pipelineStartTime", "2026-11-18T11:00:00Z")),
                row("20261118-tieA", Map.of("submittedAt", "2026-11-18T10:30:00Z"))));
        assertEquals(List.of("20261118-tieA", "20261118-tieB", "20261118-legacy",
                "20261118-older", "20261118-date"), reports.stream().map(Report::auditReportReference).toList());
        final var queued = new ReportMapperImpl().convert(reports.get(1));
        assertNull(queued.getPipelineStartTime());
        assertNull(queued.getPipelineEndTime());
        assertNull(queued.getDownloadUrl());
        verifyNoInteractions(blobServiceClient);
    }

    @Test
    void test_circulationBoundaryRejectsKeyAndUntrustedPathsEvenInCompatibilityMode() {
        final URI endpoint = URI.create(ENDPOINT);
        for (final String url : List.of(
                ENDPOINT + "/internal-audit-reports/a_pseudonymised.xlsx",
                ENDPOINT + CIRCULATION + "a_internal_with_key.xlsx",
                ENDPOINT + CIRCULATION + "a%5finternal_with_key.xlsx",
                ENDPOINT + CIRCULATION + "../a_pseudonymised.xlsx",
                ENDPOINT + CIRCULATION + "a_pseudonymised.xlsx?sig=existing",
                "https://other.blob.core.windows.net" + CIRCULATION + "a_pseudonymised.xlsx")) {
            assertTrue(ReportListing.circulationBlobName(endpoint, URI.create(url), false).isEmpty(), url);
        }
        for (final String extension : List.of(".csv", ".xlsx")) {
            final URI legacy = URI.create(ENDPOINT + CIRCULATION + "legacy" + extension);
            assertTrue(ReportListing.circulationBlobName(endpoint, legacy, false).isPresent());
            assertTrue(ReportListing.circulationBlobName(endpoint, legacy, true).isEmpty());
        }
    }

    private List<Report> reportsForUrl(final String url) {
        when(azure.blobEndpoint()).thenReturn(ENDPOINT);
        return reportsFor(List.of(row("rowKey", Map.of("pipelineStatus", "COMPLETE", "downloadUrl", url))));
    }

    private static TableEntity row(final String reference, final Map<String, Object> properties) {
        final Map<String, Object> copy = new HashMap<>(properties);
        copy.put("auditReportReference", reference);
        return new TableEntity("1", reference).setProperties(copy);
    }

    private List<Report> reportsFor(final List<TableEntity> rows) {
        underTest = new AuditReportsService(null, tableClient, mapper, azure, null, null, blobServiceClient, null);
        when(tableClient.listEntities()).thenReturn(new PagedIterable<>(() -> tableEntities));
        when(tableEntities.getElements()).thenReturn(new IterableStream<>(rows));
        return underTest.getReports();
    }
}
