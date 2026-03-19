package uk.gov.hmcts.cp.services;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.data.tables.TableClient;
import com.azure.data.tables.models.TableEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.cp.entities.input.ReportRequest;
import uk.gov.hmcts.cp.entities.output.Report;
import uk.gov.hmcts.cp.entities.output.ReportResult;
import uk.gov.hmcts.cp.properties.FabricProperties;
import uk.gov.hmcts.cp.utility.StreamUtils;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static java.util.function.Predicate.isEqual;
import static java.util.function.Predicate.not;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.cp.entities.output.ReportResult.toResult;
import static uk.gov.hmcts.cp.utility.MapUtils.*;
import static uk.gov.hmcts.cp.utility.StreamUtils.split;

@Slf4j
@Component
public record AuditReportsService(
        RestClient restClient,
        @Qualifier("reportrequests") TableClient reportRequests,
        ObjectMapper objectMapper,
        FabricProperties fabric,
        TokenRequestContext context,
        Function<TokenRequestContext, AccessToken> azureAccess
) {

    public List<Report> getReports() {

        return reportRequests.
                listEntities().
                mapPage(TableEntity::getProperties).
                mapPage(transformValues(propertyTransformers())).
                mapPage(Report.fromMap(objectMapper)).
                stream().
                toList();
    }

    private Map<String, UnaryOperator<Object>> propertyTransformers() {
        return Map.of("downloadUrl", toObjectUnaryOperator(this::toTimeLimitedUrl));
    }

    private String toTimeLimitedUrl(final String downloadUrl) {
        return downloadUrl;
    }

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
                    map(toResult(reportRequest.auditReference()));

        } catch (RuntimeException ex) {

            log.warn("RequestReport reference {} failed, status code {}", reportRequest.auditReference(), ex.getMessage());
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
