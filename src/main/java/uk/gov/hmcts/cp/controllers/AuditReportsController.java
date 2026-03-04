package uk.gov.hmcts.cp.controllers;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cp.controllers.base.SearchControllerBase;
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

@Slf4j
@RestController
public class AuditReportsController
        extends SearchControllerBase<AuditReportsService, Report, AuditReportListingItem, GetReportListing200Response>
        implements AuditReportsApi {

    private final ResultMapper resultMapper;
    private final RequestMapper requestMapper;

    public AuditReportsController(
            final AuditReportsService service,
            final RequestMapper requestMapper,
            final ResultMapper resultMapper,
            final ReportMapper reportMapper
    ) {
        super(service, reportMapper, GetReportListing200Response::new);

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

        return service.
                requestReport(requestMapper.convert(auditReportRequest)).
                map(resultMapper::convert).
                map(ResponseEntity::ok).
                orElseGet(ResponseUtils::responseInternalServerError);
    }
}
