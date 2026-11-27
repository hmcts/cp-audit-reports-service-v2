package uk.gov.hmcts.cp.services;

import com.azure.core.exception.AzureException;
import com.azure.data.tables.models.TableEntity;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.cp.properties.AzureProperties;
import uk.gov.hmcts.cp.utility.HearingId;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
record ReportListing(AzureProperties azure, BlobServiceClient blobServiceClient) {
    /* default */ static final String SUBMITTED_AT = "submittedAt";
    private static final String DOWNLOAD_URL = "downloadUrl";
    /* default */ static final String CIRCULATION_CONTAINER = "generatedreports";
    private static final Pattern BLOB_PATH = Pattern.compile("[A-Za-z0-9_./-]+");
    /* default */ static final Comparator<TableEntity> NEWEST_FIRST =
            Comparator.comparing(ReportListing::submissionTime).reversed().thenComparing(TableEntity::getRowKey);

    private static Instant submissionTime(final TableEntity entity) {
        final Map<String, Object> properties = entity.getProperties();
        return parseInstant(properties.get(SUBMITTED_AT))
                .or(() -> parseInstant(properties.get("pipelineStartTime")))
                .orElseGet(() -> referenceDate(entity.getRowKey()));
    }

    private static Instant referenceDate(final String reference) {
        // A legacy reference gives a UTC day, not a recoverable submission time.
        Instant result;
        try {
            result = reference != null && reference.length() > 8 && reference.charAt(8) == '-'
                    ? LocalDate.parse(reference.substring(0, 8), DateTimeFormatter.BASIC_ISO_DATE)
                            .atStartOfDay().toInstant(ZoneOffset.UTC)
                    : Instant.MIN;
        } catch (DateTimeParseException ignored) {
            result = Instant.MIN;
        }
        return result;
    }

    private static Optional<Instant> parseInstant(final Object value) {
        Optional<Instant> result;
        try {
            result = value instanceof String text ? Optional.of(Instant.parse(text)) : Optional.empty();
        } catch (DateTimeParseException ignored) {
            result = Optional.empty();
        }
        return result;
    }

    /* default */ Map<String, Object> safeProperties(final TableEntity entity) {
        final Map<String, Object> properties = new HashMap<>(entity.getProperties());
        properties.remove(SUBMITTED_AT);
        properties.remove("pipelineStatusDescription");
        if (!HearingId.isValid(properties.get("hearingId") instanceof String id ? id : null)) {
            properties.remove("hearingId");
        }
        if ("COMPLETE".equals(properties.get("pipelineStatus")) && properties.get(DOWNLOAD_URL) instanceof String url) {
            properties.put(DOWNLOAD_URL, sign(url).orElse(null));
        } else {
            properties.remove(DOWNLOAD_URL);
        }
        return properties;
    }

    private Optional<String> sign(final String downloadUrl) {
        Optional<String> result;
        try {
            result = circulationBlobName(URI.create(azure.blobEndpoint()), URI.create(downloadUrl),
                    azure.pseudonymisedDownloadsOnly())
                    .flatMap(blobName -> signBlob(downloadUrl, blobName));
        } catch (AzureException | IllegalArgumentException | IllegalStateException ex) {
            log.warn("Could not prepare audit report download ({})", ex.getClass().getSimpleName());
            result = Optional.empty();
        }
        return result;
    }

    /* default */ static Optional<String> circulationBlobName(
            final URI endpoint, final URI url, final boolean pseudonymisedOnly) {
        final String prefix = endpoint.getPath().replaceFirst("/$", "") + "/" + CIRCULATION_CONTAINER + "/";
        final boolean sameEndpoint = endpoint.getHost() != null && endpoint.getHost().equalsIgnoreCase(url.getHost())
                && endpoint.getScheme().equalsIgnoreCase(url.getScheme()) && endpoint.getPort() == url.getPort();
        return Optional.of(url).filter(candidate -> sameEndpoint && candidate.getUserInfo() == null
                        && candidate.getQuery() == null && candidate.getFragment() == null
                        && candidate.getRawPath().startsWith(prefix))
                .map(candidate -> candidate.getRawPath().substring(prefix.length()))
                .filter(name -> isAllowedBlobName(name, pseudonymisedOnly));
    }

    private static boolean isAllowedBlobName(final String name, final boolean pseudonymisedOnly) {
        final String lower = name.toLowerCase(Locale.ROOT);
        return BLOB_PATH.matcher(name).matches()
                && Arrays.stream(name.split("/", -1)).noneMatch(s -> s.isEmpty() || ".".equals(s) || "..".equals(s))
                && !lower.contains("_internal_with_key")
                && isAllowedReport(lower, pseudonymisedOnly);
    }

    private static boolean isAllowedReport(final String name, final boolean pseudonymisedOnly) {
        return (name.startsWith("report/generated/") && name.endsWith("_pseudonymised.xlsx"))
                || (!pseudonymisedOnly && (name.endsWith(".csv") || name.endsWith(".xlsx")));
    }

    private Optional<String> signBlob(final String url, final String name) {
        final BlobClient blob = blobServiceClient.getBlobContainerClient(CIRCULATION_CONTAINER).getBlobClient(name);
        final OffsetDateTime start = OffsetDateTime.now(ZoneOffset.UTC);
        final OffsetDateTime end = start.plusMinutes(azure.downloadUrlMinutesValid());
        final UserDelegationKey key = blobServiceClient.getUserDelegationKey(start, end);
        final String sas = blob.generateUserDelegationSas(
                new BlobServiceSasSignatureValues(end, BlobSasPermission.parse("r")), key);
        return sas == null || sas.isBlank() ? Optional.empty() : Optional.of(url + "?" + sas);
    }
}
