package uk.gov.hmcts.cp.services;

import com.azure.core.util.Context;
import com.azure.data.tables.TableClient;
import com.azure.data.tables.models.TableEntity;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobStorageException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.cp.properties.AzureProperties;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class ReportBundleServiceTest {
    private static final String REFERENCE = "20261127-abC123";
    private static final String ENDPOINT = "https://example.blob.core.windows.net";
    private static final String STEM = "report/generated/2026/11/27/report_" + REFERENCE + "_120000_000001_pseudonymised";
    private static final String URL = ENDPOINT + "/generatedreports/" + STEM + ".xlsx";

    @Mock
    private TableClient table;
    @Mock
    private AzureProperties azure;
    @Mock
    private BlobServiceClient blobs;

    @Test
    void bundlesTheExactPairWithNoKeyLink() throws Exception {
        row("COMPLETE", URL);
        final BlobClient report = blob("generatedreports", STEM + ".xlsx");
        final BlobClient key = blob("internal-audit-reports", STEM + "_internal_with_key.xlsx");
        workbook(report, "report");
        workbook(key, "key");

        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(service().download(REFERENCE)))) {
            assertEquals("pseudonymised-report.xlsx", zip.getNextEntry().getName());
            assertEquals("report", new String(zip.readAllBytes(), StandardCharsets.UTF_8));
            assertEquals("internal-report-with-identity-key.xlsx", zip.getNextEntry().getName());
            assertEquals("key", new String(zip.readAllBytes(), StandardCharsets.UTF_8));
            assertNull(zip.getNextEntry());
        }
    }

    @Test
    void missingWorkbookDoesNotReturnAnArchive() {
        row("COMPLETE", URL);
        final BlobClient report = blob("generatedreports", STEM + ".xlsx");
        final BlobClient key = blob("internal-audit-reports", STEM + "_internal_with_key.xlsx");
        when(report.getProperties()).thenReturn(mock(BlobProperties.class));
        final BlobStorageException missing = mock(BlobStorageException.class);
        when(missing.getStatusCode()).thenReturn(404);
        when(key.getProperties()).thenThrow(missing);

        assertEquals(NOT_FOUND, assertThrows(ResponseStatusException.class,
                () -> service().download(REFERENCE)).getStatusCode());
    }

    @Test
    void doesNotReadTheWorkbookFromAnotherReport() {
        row("COMPLETE", URL.replace(REFERENCE, "20261127-other1"));
        assertEquals(NOT_FOUND, assertThrows(ResponseStatusException.class,
                () -> service().download(REFERENCE)).getStatusCode());
        verifyNoInteractions(blobs);
    }

    @Test
    void doesNotUseStaleDownloadFromFailedRequest() {
        when(table.getEntity("1", REFERENCE)).thenReturn(new TableEntity("1", REFERENCE)
                .addProperty("pipelineStatus", "FAILED").addProperty("downloadUrl", URL));
        assertEquals(NOT_FOUND, assertThrows(ResponseStatusException.class,
                () -> service().download(REFERENCE)).getStatusCode());
        verifyNoInteractions(blobs);
    }

    @Test
    void validatesReferenceBeforeStorageLookup() {
        assertEquals(BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> service().download("invalid")).getStatusCode());
        verifyNoInteractions(table, blobs);
    }

    private ReportBundleService service() {
        return new ReportBundleService(table, blobs, azure);
    }

    private void row(final String status, final String url) {
        when(azure.blobEndpoint()).thenReturn(ENDPOINT);
        when(table.getEntity("1", REFERENCE)).thenReturn(new TableEntity("1", REFERENCE)
                .addProperty("pipelineStatus", status).addProperty("downloadUrl", url));
    }

    private BlobClient blob(final String container, final String name) {
        final BlobContainerClient bucket = mock(BlobContainerClient.class);
        final BlobClient client = mock(BlobClient.class);
        when(blobs.getBlobContainerClient(container)).thenReturn(bucket);
        when(bucket.getBlobClient(name)).thenReturn(client);
        return client;
    }

    private static void workbook(final BlobClient blob, final String text) {
        final byte[] contents = text.getBytes(StandardCharsets.UTF_8);
        final BlobProperties properties = mock(BlobProperties.class);
        when(properties.getBlobSize()).thenReturn((long) contents.length);
        when(properties.getETag()).thenReturn("revision");
        when(blob.getProperties()).thenReturn(properties);
        doAnswer(call -> {
            call.<OutputStream>getArgument(0).write(contents);
            return null;
        }).when(blob).downloadStreamWithResponse(any(), isNull(), isNull(),
                argThat(condition -> "revision".equals(condition.getIfMatch())), eq(false),
                any(Duration.class), eq(Context.NONE));
    }
}
