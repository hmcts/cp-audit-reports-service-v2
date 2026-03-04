package uk.gov.hmcts.cp.http;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.cp.http.base.ApiTestBase;
import uk.gov.hmcts.cp.openapi.model.AuditReportRequest;
import uk.gov.hmcts.cp.openapi.model.GetReportListing200Response;
import uk.gov.hmcts.cp.openapi.model.PostReportRequest202Response;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.cp.openapi.model.AuditReportListingItem.PipelineStatusEnum.PENDING;
import static uk.gov.hmcts.cp.openapi.model.AuditReportRequest.SearchCriteriaEnum.ALL_ACTIVITY;

class AuditReportsApiTest extends ApiTestBase<GetReportListing200Response> {

    public AuditReportsApiTest() {
        super(GetReportListing200Response.class);
    }

    @Test
    void get_reports_endpoint_returns_preloaded_test_data() {

        // When
        final ResponseEntity<GetReportListing200Response> response = get("/reports");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getReports().size());
        assertNotNull(response.getBody().getReports().getFirst());
        assertEquals("550e8400-e29b-41d4-a716-446655440000", response.getBody().getReports().getFirst().getPipelineJobId().toString());
        assertEquals(PENDING, response.getBody().getReports().getFirst().getPipelineStatus());
    }

    @Test
    void submit_report_request() {

        // Given
        final AuditReportRequest request = new AuditReportRequest(
                LocalDate.now().minusDays(2),
                LocalDate.now().minusDays(1),
                true,
                "no@where.com",
                ALL_ACTIVITY,
                "abc",
                "123,456",
                "789"
        );

        // When
        final ResponseEntity<PostReportRequest202Response> response = post("/reports", request, PostReportRequest202Response.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("12345", response.getBody().getPipelineJobId());
        assertEquals(15, response.getBody().getAuditReportReference().length());
    }
}
