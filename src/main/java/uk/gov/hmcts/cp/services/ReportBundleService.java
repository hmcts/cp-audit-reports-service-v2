package uk.gov.hmcts.cp.services;

import com.azure.core.util.Context;
import com.azure.data.tables.TableClient;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableServiceException;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobRequestConditions;
import com.azure.storage.blob.models.BlobStorageException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.cp.properties.AzureProperties;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.regex.Pattern;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.PAYLOAD_TOO_LARGE;
import static org.springframework.http.HttpStatus.PRECONDITION_FAILED;

@Service
public record ReportBundleService(TableClient reportRequests, BlobServiceClient blobs, AzureProperties azure) {
    private static final Pattern REFERENCE = Pattern.compile("[0-9]{8}-[A-Za-z0-9]{6}");
    private static final String KEY_CONTAINER = "internal-audit-reports";
    private static final long MAX_WORKBOOK_BYTES = 32L * 1024L * 1024L;
    private static final Duration BLOB_TIMEOUT = Duration.ofSeconds(30);

    public byte[] download(final String reference) throws IOException {
        if (!REFERENCE.matcher(reference).matches()) {
            throw new ResponseStatusException(BAD_REQUEST, "Invalid audit report reference");
        }

        return downloadPair(reference);
    }

    private byte[] downloadPair(final String reference) throws IOException {
        final String path;
        final String keyPath;
        final BlobClient report;
        final BlobClient internal;
        path = circulationPath(reference);
        keyPath = path.substring(0, path.length() - ".xlsx".length()) + "_internal_with_key.xlsx";
        report = blobs.getBlobContainerClient(ReportListing.CIRCULATION_CONTAINER).getBlobClient(path);
        internal = blobs.getBlobContainerClient(KEY_CONTAINER).getBlobClient(keyPath);
        return bundle(report, internal);
    }

    private String circulationPath(final String reference) {
        try {
            final TableEntity row;
            row = reportRequests.getEntity(AuditReportsService.PARTITION, reference);
            if (!"COMPLETE".equals(row.getProperty("pipelineStatus"))
                    || !(row.getProperty("downloadUrl") instanceof String url)) {
                throw unavailable();
            }
            return validatedCirculationPath(reference, url);
        } catch (TableServiceException ex) {
            if (ex.getResponse().getStatusCode() == NOT_FOUND.value()) {
                throw unavailable();
            }
            throw ex;
        } catch (IllegalArgumentException ex) {
            throw unavailable();
        }
    }

    private String validatedCirculationPath(final String reference, final String url) {
        final String pattern;
        pattern = "report/generated/[0-9]{4}/[0-9]{2}/[0-9]{2}/report_"
                + Pattern.quote(reference) + "_[0-9]{6}_[0-9]{6}_pseudonymised\\.xlsx";
        return ReportListing.circulationBlobName(URI.create(azure.blobEndpoint()), URI.create(url), true)
                .filter(name -> name.matches(pattern))
                .orElseThrow(ReportBundleService::unavailable);
    }

    private static byte[] bundle(final BlobClient report, final BlobClient internal) throws IOException {
        try {
            final BlobProperties reportInfo;
            final BlobProperties internalInfo;
            reportInfo = report.getProperties();
            internalInfo = internal.getProperties();
            if (reportInfo.getBlobSize() > MAX_WORKBOOK_BYTES
                    || internalInfo.getBlobSize() > MAX_WORKBOOK_BYTES - reportInfo.getBlobSize()) {
                throw new ResponseStatusException(PAYLOAD_TOO_LARGE, "Audit workbooks exceed the bundle size limit");
            }
            return writeArchive(report, reportInfo, internal, internalInfo);
        } catch (BlobStorageException ex) {
            if (ex.getStatusCode() == NOT_FOUND.value() || ex.getStatusCode() == PRECONDITION_FAILED.value()) {
                throw unavailable();
            }
            throw ex;
        }
    }

    private static byte[] writeArchive(final BlobClient report, final BlobProperties reportInfo,
                                       final BlobClient internal, final BlobProperties internalInfo) throws IOException {
        final ByteArrayOutputStream buffer;
        buffer = new ByteArrayOutputStream((int) (reportInfo.getBlobSize() + internalInfo.getBlobSize()) + 4096);
        // Finish the archive before sending any content, so storage errors cannot produce a partial download.
        try (ZipOutputStream zip = new ZipOutputStream(buffer)) {
            zip.setLevel(Deflater.NO_COMPRESSION);
            addWorkbook(zip, report, reportInfo, "pseudonymised-report.xlsx");
            addWorkbook(zip, internal, internalInfo, "internal-report-with-identity-key.xlsx");
        }
        return buffer.toByteArray();
    }

    private static void addWorkbook(final ZipOutputStream zip, final BlobClient blob, final BlobProperties info,
                                    final String name) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        blob.downloadStreamWithResponse(zip, null, null, new BlobRequestConditions().setIfMatch(info.getETag()),
                false, BLOB_TIMEOUT, Context.NONE);
        zip.closeEntry();
    }

    private static ResponseStatusException unavailable() {
        return new ResponseStatusException(NOT_FOUND, "Audit workbook bundle unavailable");
    }
}
