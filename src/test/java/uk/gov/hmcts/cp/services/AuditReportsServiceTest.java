package uk.gov.hmcts.cp.services;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.exception.AzureException;
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
import uk.gov.hmcts.cp.properties.AzureProperties;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuditReportsServiceTest {

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
        when(tableEntities.getValue()).thenReturn(List.of(
                new TableEntity("partitionKey", "rowKey").
                        setProperties(Map.of(
                                "userEmail", "no@where.com",
                                "pipelineJobId", "12345"
                        ))
        ));

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
        final String url = "https://audit.blob.core.windows.net/generatedreports/report.csv";
        when(azure.segmentsToSkip()).thenReturn(3);
        when(azure.downloadUrlMinutesValid()).thenReturn(10);
        when(blobServiceClient.getBlobContainerClient("generatedreports")).thenReturn(blobContainerClient);
        when(blobContainerClient.getBlobClient("report.csv")).thenReturn(blobClient);
        when(blobServiceClient.getUserDelegationKey(any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(delegationKey);
        when(blobClient.generateUserDelegationSas(any(BlobServiceSasSignatureValues.class), eq(delegationKey)))
                .thenReturn("synthetic-test-signature");

        assertEquals(url + "?synthetic-test-signature", reportsForUrl(url).getFirst().downloadUrl());
    }

    @Test
    void test_getReports_preservesUrlWhenSigningFails() {
        final String url = "https://audit.blob.core.windows.net/generatedreports/report.csv";
        when(azure.segmentsToSkip()).thenReturn(3);
        when(blobServiceClient.getBlobContainerClient("generatedreports"))
                .thenThrow(new IllegalStateException("Synthetic storage failure"));

        assertEquals(url, reportsForUrl(url).getFirst().downloadUrl());
    }

    @Test
    void test_getReports_preservesUrlWhenStorageFails() {
        final String url = "https://audit.blob.core.windows.net/generatedreports/report.csv";
        when(azure.segmentsToSkip()).thenReturn(3);
        when(blobServiceClient.getBlobContainerClient("generatedreports"))
                .thenThrow(new AzureException("Synthetic Azure failure"));

        assertEquals(url, reportsForUrl(url).getFirst().downloadUrl());
    }

    @Test
    void test_getReports_preservesMalformedUrl() {
        when(azure.segmentsToSkip()).thenReturn(3);

        assertEquals("invalid-url", reportsForUrl("invalid-url").getFirst().downloadUrl());
        verifyNoInteractions(blobServiceClient);
    }

    private List<Report> reportsForUrl(final String url) {
        underTest = new AuditReportsService(null, tableClient, mapper, azure, null, null, blobServiceClient, null);
        when(tableClient.listEntities()).thenReturn(new PagedIterable<>(() -> tableEntities));
        when(tableEntities.getValue()).thenReturn(List.of(
                new TableEntity("partitionKey", "rowKey").setProperties(Map.of("downloadUrl", url))
        ));
        return underTest.getReports();
    }
}
