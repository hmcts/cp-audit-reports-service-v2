package uk.gov.hmcts.cp.controllers;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.cp.openapi.api.AuditReportsApi;
import uk.gov.hmcts.cp.openapi.model.AuditReportRequest;
import uk.gov.hmcts.cp.openapi.model.GetReportListing200Response;
import uk.gov.hmcts.cp.openapi.model.PostReportRequest202Response;

public class AuditReportsController implements AuditReportsApi {

    @Override
    public ResponseEntity<PostReportRequest202Response> postReportRequest(@Valid AuditReportRequest auditReportRequest) {
        return AuditReportsApi.super.postReportRequest(auditReportRequest);
    }

    @Override
    public ResponseEntity<GetReportListing200Response> getReportListing() {
        return AuditReportsApi.super.getReportListing();
    }
}
