package uk.gov.hmcts.cp.http;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.cp.http.base.ApiTestBase;
import uk.gov.hmcts.cp.openapi.model.AuditReportRequest;
import uk.gov.hmcts.cp.openapi.model.GetReportListing200Response;
import uk.gov.hmcts.cp.openapi.model.PostReportRequest202Response;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.cp.openapi.model.AuditReportListingItem.PipelineStatusEnum.PENDING;
import static uk.gov.hmcts.cp.openapi.model.AuditReportRequest.SearchCriteriaEnum.ALL_ACTIVITY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AuditReportsApiTest extends ApiTestBase<GetReportListing200Response> {

    public AuditReportsApiTest() {
        super(GetReportListing200Response.class);
    }

    @Test
    void submit_valid_report_request() {

        // Given
        final AuditReportRequest request = new AuditReportRequest(
                LocalDate.now().minusDays(2),
                LocalDate.now().minusDays(1),
                false,
                "no@where.com",
                ALL_ACTIVITY,
                "abc",
                "123,456",
                "789"
        );

        final HttpHeaders headers = httpHeaders(Map.of("CJSCPPUID", "audit"));

        // When
        final ResponseEntity<PostReportRequest202Response> response = post("/reports", request, headers, PostReportRequest202Response.class);

        // Then
        assertEquals(OK, response.getStatusCode());
        assertEquals("12345", response.getBody().getPipelineJobId());
        assertEquals(15, response.getBody().getAuditReportReference().length());
    }

    @Test
    void submit_invalid_report_request() {

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

        final HttpHeaders headers = httpHeaders(Map.of("CJSCPPUID", "audit"));

        // When
        final HttpClientErrorException.BadRequest exception = assertThrows(
                HttpClientErrorException.BadRequest.class, () -> post("/reports", request, headers));

        // Then
        assertEquals(BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void get_reports_endpoint_returns_preloaded_test_data() {

        // When
        final ResponseEntity<GetReportListing200Response> response = get("/reports");

        // Then
        assertEquals(OK, response.getStatusCode());
        assertEquals(1, response.getBody().getReports().size());
        assertNotNull(response.getBody().getReports().getFirst());
        assertEquals("550e8400-e29b-41d4-a716-446655440000", response.getBody().getReports().getFirst().getPipelineJobId().toString());
        assertEquals(PENDING, response.getBody().getReports().getFirst().getPipelineStatus());
    }
}
