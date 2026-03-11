package uk.gov.hmcts.cp.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.cp.controllers.base.SearchControllerBase;
import uk.gov.hmcts.cp.entities.input.ReportRequest;
import uk.gov.hmcts.cp.entities.output.Report;
import uk.gov.hmcts.cp.mappers.ReportMapper;
import uk.gov.hmcts.cp.mappers.RequestMapper;
import uk.gov.hmcts.cp.mappers.ResultMapper;
import uk.gov.hmcts.cp.openapi.api.AuditReportsApi;
import uk.gov.hmcts.cp.openapi.model.AuditReportListingItem;
import uk.gov.hmcts.cp.openapi.model.AuditReportRequest;
import uk.gov.hmcts.cp.openapi.model.GetReportListing200Response;
import uk.gov.hmcts.cp.openapi.model.PostReportRequest202Response;
import uk.gov.hmcts.cp.services.AuditReportsService;
import uk.gov.hmcts.cp.utility.ResponseUtils;

import java.util.Optional;
import java.util.function.Function;

@Slf4j
@RestController
public class AuditReportsController
        extends SearchControllerBase<AuditReportsService, Report, AuditReportListingItem, GetReportListing200Response>
        implements AuditReportsApi {

    private final ResultMapper resultMapper;
    private final RequestMapper requestMapper;

    public AuditReportsController(
            final AuditReportsService reportService,
            final RequestMapper requestMapper,
            final ResultMapper resultMapper,
            final ReportMapper reportMapper
    ) {
        super(reportService, reportMapper, GetReportListing200Response::new);

        this.requestMapper = requestMapper;
        this.resultMapper = resultMapper;
    }

    @Override
    public ResponseEntity<GetReportListing200Response> getReportListing() {

        log.info("getReportListing");
        return responseOk(service.getReports());
    }

    @Override
    public ResponseEntity<PostReportRequest202Response> postReportRequest(
            @Valid final AuditReportRequest auditReportRequest
    ) {
        log.info("postReportRequest");

        return getHeader("CJSCPPUID").
                flatMap(cjsCppUid -> convertRequest(auditReportRequest, cjsCppUid)).
                map(reportRequest -> service.
                        requestReport(reportRequest).
                        map(resultMapper::convert).
                        map(ResponseEntity::ok).
                        orElseGet(ResponseUtils::responseInternalServerError)).
                orElseGet(ResponseUtils::responseBadRequest);
    }

    protected Optional<String> getHeader(final String headerName) {

        return Optional.ofNullable(RequestContextHolder.getRequestAttributes()).
                map(request -> (ServletRequestAttributes) request).
                map(ServletRequestAttributes::getRequest).
                flatMap(requestHeader(headerName));
    }

    @SuppressWarnings("PMD")
    private Optional<ReportRequest> convertRequest(
            final AuditReportRequest auditReportRequest,
            final String cjsCppUid
    ) {
        try {
            return Optional.of(requestMapper.convert(auditReportRequest, cjsCppUid));

        } catch (IllegalArgumentException ex) {

            log.warn("Invalid audit request");
            return Optional.empty();
        }
    }

    private static Function<HttpServletRequest, Optional<String>> requestHeader(final String headerName) {
        return request -> Optional.ofNullable(request.getHeader(headerName));
    }
}
