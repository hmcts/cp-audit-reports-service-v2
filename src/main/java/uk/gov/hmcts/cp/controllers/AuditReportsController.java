package uk.gov.hmcts.cp.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.cp.entities.input.ReportRequest;
import uk.gov.hmcts.cp.mappers.RequestMapper;
import uk.gov.hmcts.cp.mappers.ResultMapper;
import uk.gov.hmcts.cp.openapi.api.AuditReportsApi;
import uk.gov.hmcts.cp.openapi.model.AuditReportRequest;
import uk.gov.hmcts.cp.openapi.model.PostReportRequest202Response;
import uk.gov.hmcts.cp.services.AuditReportsService;
import uk.gov.hmcts.cp.services.UserSearchService;
import uk.gov.hmcts.cp.utility.ResponseUtils;

import java.util.Optional;
import java.util.function.Function;

@Slf4j
@RestController
public class AuditReportsController implements AuditReportsApi {

    private final ResultMapper resultMapper;
    private final RequestMapper requestMapper;
    private final AuditReportsService reportService;
    private final UserSearchService userService;

    public AuditReportsController(
            final AuditReportsService reportService,
            final UserSearchService userService,
            final RequestMapper requestMapper,
            final ResultMapper resultMapper
    ) {
        this.requestMapper = requestMapper;
        this.resultMapper = resultMapper;
        this.reportService = reportService;
        this.userService = userService;
    }

    @Override
    public ResponseEntity<PostReportRequest202Response> postReportRequest(
            @Valid final AuditReportRequest auditReportRequest
    ) {
        log.info("postReportRequest");

        return getHeader("CJSCPPUID").
                flatMap(cjsCppUid -> convertRequest(auditReportRequest, cjsCppUid)).
                map(reportRequest -> reportService.
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
