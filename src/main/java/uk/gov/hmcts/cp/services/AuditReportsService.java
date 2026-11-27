package uk.gov.hmcts.cp.services;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.util.Context;
import com.azure.data.tables.TableClient;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableEntityUpdateMode;
import com.azure.data.tables.models.TableServiceException;
import com.azure.storage.blob.BlobServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.cp.entities.input.ReportRequest;
import uk.gov.hmcts.cp.entities.output.Report;
import uk.gov.hmcts.cp.entities.output.ReportResult;
import uk.gov.hmcts.cp.properties.AzureProperties;
import uk.gov.hmcts.cp.properties.FabricProperties;
import uk.gov.hmcts.cp.utility.StreamUtils;

import java.net.URI;
import java.net.UnknownHostException;
import java.nio.channels.UnresolvedAddressException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.cp.entities.output.ReportResult.toResult;
import static uk.gov.hmcts.cp.utility.StreamUtils.split;

@Slf4j
@Component
public record AuditReportsService(
        RestClient restClient,
        TableClient reportRequests,
        ObjectMapper objectMapper,
        AzureProperties azure,
        FabricProperties fabric,
        TokenRequestContext context,
        BlobServiceClient  blobServiceClient,
        Function<TokenRequestContext, AccessToken> azureAccess
) {
    /* default */ static final String PARTITION = "1";

    public List<Report> getReports() {
        final ReportListing listing = new ReportListing(azure, blobServiceClient);
        return reportRequests.listEntities().stream()
                .sorted(ReportListing.NEWEST_FIRST)
                .map(listing::safeProperties)
                .map(Report.fromMap(objectMapper))
                .toList();
    }

    @SuppressWarnings("PMD.OnlyOneReturn")
    public Optional<ReportResult> requestReport(final ReportRequest reportRequest) {
        final String reference = reportRequest.auditReportReference();
        final String etag;
        try {
            etag = createPendingRequest(reportRequest);
        } catch (RuntimeException ex) {
            log.warn("Could not create audit request {}: {}", reference, ex.getClass().getSimpleName());
            return Optional.empty();
        }

        return dispatch(reportRequest, etag);
    }

    @SuppressWarnings("PMD.OnlyOneReturn")
    private Optional<ReportResult> dispatch(final ReportRequest reportRequest, final String etag) {
        final String reference = reportRequest.auditReportReference();
        final String token;
        try {
            token = Optional.ofNullable(azureAccess.apply(context))
                    .map(AccessToken::getToken)
                    .filter(StringUtils::hasText)
                    .orElseThrow(() -> new IllegalStateException("No Fabric access token"));
        } catch (RuntimeException ex) {
            log.warn("Fabric token acquisition failed for audit request {}: {}", reference, ex.getClass().getSimpleName());
            markDispatchFailed(reference, etag);
            return Optional.empty();
        }

        final ResponseEntity<Void> response;
        try {
            response = submitToFabric(reportRequest, token);
        } catch (ResourceAccessException ex) {
            if (ex.contains(UnknownHostException.class) || ex.contains(UnresolvedAddressException.class)) {
                log.warn("Fabric DNS resolution failed for audit request {}", reference);
                markDispatchFailed(reference, etag);
            } else {
                logUnknownAcceptance(reference, ex.getMostSpecificCause());
            }
            return Optional.empty();
        } catch (RuntimeException ex) {
            logUnknownAcceptance(reference, ex);
            return Optional.empty();
        }

        if (!response.getStatusCode().isSameCodeAs(ACCEPTED)) {
            final int status = response.getStatusCode().value();
            log.warn("Fabric returned HTTP {} for audit request {}", status, reference);
            if (definitiveRejection(status)) {
                markDispatchFailed(reference, etag);
            }
            return Optional.empty();
        }

        return acceptedResult(reference, response.getHeaders());
    }

    private static void logUnknownAcceptance(final String reference, final Throwable cause) {
        log.warn("Fabric acceptance unknown for audit request {}: {}", reference, cause.getClass().getSimpleName());
    }

    private static Optional<ReportResult> acceptedResult(final String reference, final HttpHeaders headers) {
        Optional<ReportResult> result;
        try {
            result = Optional.ofNullable(headers.getLocation())
                    .map(URI::getPath)
                    .map(split("/"))
                    .flatMap(StreamUtils::last)
                    .map(toResult(reference));
            if (result.isEmpty()) {
                log.warn("Fabric accepted audit request {} but did not return a job location", reference);
            }
        } catch (IllegalArgumentException ex) {
            log.warn("Fabric accepted audit request {} but returned an invalid job location", reference);
            result = Optional.empty();
        }
        return result;
    }

    private String createPendingRequest(final ReportRequest reportRequest) {
        final Map<String, Object> props = objectMapper.convertValue(reportRequest, new TypeReference<>() {});
        props.put("downloadUrl", "");
        props.put("pipelineStatus", "PENDING");
        props.put(ReportListing.SUBMITTED_AT, Instant.now().toString());

        final TableEntity row = new TableEntity(PARTITION, reportRequest.auditReportReference()).setProperties(props);
        final String etag = reportRequests.createEntityWithResponse(row, null, Context.NONE)
                .getHeaders().getValue(HttpHeaderName.ETAG);
        // Azure Tables' TableScrubEtagPolicy drops the closing quote from weak ETags.
        return etag != null && etag.startsWith("W/\"") && !etag.endsWith("\"") ? etag + "\"" : etag;
    }

    private ResponseEntity<Void> submitToFabric(final ReportRequest reportRequest, final String token) {
        return restClient.mutate().defaultHeaders(HttpHeaders::clear).build().post()
                .uri(fabric.baseUrl() + fabric.path(), fabricParams())
                .headers(headers -> headers.setBearerAuth(token))
                .accept(APPLICATION_JSON)
                .body(Map.of("executionData", Map.of("parameters", reportRequest)))
                .exchange((_, response) -> new ResponseEntity<>(response.getHeaders(), response.getStatusCode()));
    }

    private static boolean definitiveRejection(final int status) {
        // Timeouts, conflicts and server errors do not tell us whether a job started.
        return switch (status) {
            case 400, 401, 403, 404, 405, 415, 422, 429 -> true;
            default -> false;
        };
    }

    private void markDispatchFailed(final String reference, final String etag) {
        if (!StringUtils.hasText(etag) || "*".equals(etag)) {
            log.warn("Cannot conditionally mark audit request {} failed: missing row version", reference);
            return;
        }

        final TableEntity failed = new TableEntity(PARTITION, reference)
                .addProperty("odata.etag", etag)
                .addProperty("pipelineStatus", "FAILED")
                .addProperty("downloadUrl", "");
        try {
            reportRequests.updateEntityWithResponse(failed, TableEntityUpdateMode.MERGE, true, null, Context.NONE);
        } catch (TableServiceException ex) {
            log.warn("Could not conditionally mark audit request {} failed: storage HTTP {}",
                    reference, ex.getResponse().getStatusCode());
        } catch (RuntimeException ex) {
            log.warn("Could not conditionally mark audit request {} failed: {}", reference, ex.getClass().getSimpleName());
        }
    }

    private Map<String, String> fabricParams() {
        return Map.of(
                "ITEM_ID", fabric.itemId(),
                "WORKSPACE_ID", fabric.workspaceId()
        );
    }
}
