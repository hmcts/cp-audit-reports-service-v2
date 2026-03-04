package uk.gov.hmcts.cp.services;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.cp.entities.input.ReportRequest;
import uk.gov.hmcts.cp.entities.output.ReportResult;
import uk.gov.hmcts.cp.properties.FabricProperties;
import uk.gov.hmcts.cp.utility.StreamUtils;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.util.function.Predicate.isEqual;
import static java.util.function.Predicate.not;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.cp.entities.output.ReportResult.toResult;
import static uk.gov.hmcts.cp.utility.StreamUtils.split;

@Slf4j
@Component
public record AuditReportsService(
        RestClient restClient,
        FabricProperties fabric,
        TokenRequestContext context,
        Function<TokenRequestContext, AccessToken> azureAccess
) {

    @SuppressWarnings("PMD")
    public Optional<ReportResult> requestReport(final ReportRequest reportRequest) {

        try {
            return Optional.
                    ofNullable(restClient.
                            post().
                            uri(fabric.baseUrl() + fabric.path(), fabricParams()).
                            headers(HttpHeaders::clear).
                            headers(this::setBearerAuth).
                            accept(APPLICATION_JSON).
                            body(Map.of("executionData", reportRequest)).
                            retrieve().
                            onStatus(not(isEqual(ACCEPTED)), (_, res) -> {
                                throw new RuntimeException(String.valueOf(res.getStatusCode().value()));
                            }).
                            toBodilessEntity().
                            getHeaders().
                            getLocation()).
                    map(URI::getPath).
                    map(split("/")).
                    flatMap(StreamUtils::last).
                    map(toResult(reportRequest.auditReportReference()));

        } catch (RuntimeException ex) {

            log.warn("RequestReport reference {} failed, status code {}", reportRequest.auditReportReference(), ex.getMessage());
            return Optional.empty();
        }
    }

    private void setBearerAuth(final HttpHeaders headers) {
        headers.setBearerAuth(azureAccess.apply(context).getToken());
    }

    private Map<String, String> fabricParams() {
        return Map.of(
                "ITEM_ID", fabric.itemId(),
                "WORKSPACE_ID", fabric.workspaceId()
        );
    }
}
