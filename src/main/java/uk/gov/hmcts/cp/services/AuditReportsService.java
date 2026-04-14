package uk.gov.hmcts.cp.services;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.data.tables.TableClient;
import com.azure.data.tables.models.TableEntity;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
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
import java.time.OffsetDateTime;
import java.util.Arrays;
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
        TableClient reportRequests,
        ObjectMapper objectMapper,
        AzureProperties azure,
        FabricProperties fabric,
        TokenRequestContext context,
        BlobServiceClient  blobServiceClient,
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

        try {

            final List<String> segments = Arrays.stream(downloadUrl.replace(azure.blobEndpoint(), "").split("/")).toList();

            final String container = segments.getFirst();
            final String blobName = Strings.join(segments.stream().skip(1).toList(), '/');

            final BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(container);
            final BlobClient blobClient = blobContainerClient.getBlobClient(blobName);

            final OffsetDateTime keyStart = OffsetDateTime.now();
            final OffsetDateTime keyEnd = keyStart.plusMinutes(azure.downloadUrlMinutesValid());

            final String sasToken = blobClient.generateUserDelegationSas(
                    new BlobServiceSasSignatureValues(keyEnd, BlobSasPermission.parse("r")),
                    blobServiceClient.getUserDelegationKey(keyStart, keyEnd)
            );

            return downloadUrl + "?" + sasToken;

        } catch (Exception ex) {

            log.warn("Failed to generate time limited downloadUrl", ex);
            return downloadUrl;
        }
    }

    @SuppressWarnings("PMD")
    public Optional<ReportResult> requestReport(final ReportRequest reportRequest) {

        try {
            final Map<String, Object> props = objectMapper.convertValue(
                    reportRequest, new TypeReference<>() {}
            );

            props.put("downloadUrl", "http://localhost");
            props.put("pipelineStatus", "PENDING");

            reportRequests.createEntity(
                    new TableEntity("1", reportRequest.auditReportReference()).setProperties(props)
            );

            return Optional.
                    ofNullable(restClient.
                            post().
                            uri(fabric.baseUrl() + fabric.path(), fabricParams()).
                            headers(HttpHeaders::clear).
                            headers(this::setBearerAuth).
                            accept(APPLICATION_JSON).
                            body(Map.of("executionData", Map.of("parameters", reportRequest))).
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
