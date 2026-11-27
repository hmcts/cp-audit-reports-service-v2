package uk.gov.hmcts.cp.http;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.cp.http.base.ApiTestBase;
import uk.gov.hmcts.cp.openapi.model.AuditReportListingItem;
import uk.gov.hmcts.cp.openapi.model.AuditReportRequest;
import uk.gov.hmcts.cp.openapi.model.GetReportListing200Response;
import uk.gov.hmcts.cp.openapi.model.PostReportRequest202Response;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.cp.openapi.model.AuditReportListingItem.PipelineStatusEnum.PENDING;
import static uk.gov.hmcts.cp.openapi.model.AuditReportRequest.SearchCriteriaEnum.ALL_ACTIVITY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuditReportsApiTest extends ApiTestBase<GetReportListing200Response> {

    private String submittedReference;

    public AuditReportsApiTest() {
        super(GetReportListing200Response.class);
    }

    @Test
    @Order(1)
    void submit_valid_report_request() {

        // Given
        final AuditReportRequest request = new AuditReportRequest(
                LocalDate.now().minusDays(2),
                LocalDate.now().minusDays(1),
                false,
                "no@where.com",
                ALL_ACTIVITY,
                "abc",
                "00000000-0000-0000-0000-000000000001,00000000-0000-0000-0000-000000000002",
                UUID.randomUUID().toString()
        );

        final HttpHeaders headers = httpHeaders(Map.of("CJSCPPUID", "00000000-0000-0000-0000-000000000000"));

        // When
        final ResponseEntity<PostReportRequest202Response> response = post("/reports", request, headers, PostReportRequest202Response.class);

        // Then
        assertEquals(OK, response.getStatusCode());
        assertEquals("12345", response.getBody().getPipelineJobId());
        submittedReference = response.getBody().getAuditReportReference();
        assertEquals(15, submittedReference.length());
    }

    @Test
    @Order(2)
    void submit_invalid_report_request() {

        // Given
        final AuditReportRequest request = new AuditReportRequest(
                LocalDate.now().minusDays(2),
                LocalDate.now().minusDays(1),
                true,
                "no@where.com",
                ALL_ACTIVITY,
                "abc",
                "00000000-0000-0000-0000-000000000001,00000000-0000-0000-0000-000000000002",
                UUID.randomUUID().toString()
        );

        final HttpHeaders headers = httpHeaders(Map.of("CJSCPPUID", "00000000-0000-0000-0000-000000000000"));

        // When
        final HttpClientErrorException.BadRequest exception = assertThrows(
                HttpClientErrorException.BadRequest.class, () -> post("/reports", request, headers));

        // Then
        assertEquals(BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    @Order(3)
    void get_reports_endpoint_lists_newly_submitted_report_before_historical_report() {

        // When
        final ResponseEntity<GetReportListing200Response> response = get("/reports");

        // Then
        assertEquals(OK, response.getStatusCode());
        assertNotNull(response.getBody());
        final List<AuditReportListingItem> reports = response.getBody().getReports();
        assertEquals(2, reports.size());
        assertNotNull(submittedReference);
        assertEquals(submittedReference, reports.getFirst().getAuditReportReference());
        assertNull(reports.getFirst().getPipelineJobId());
        assertEquals(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"), reports.getLast().getPipelineJobId());
        for (final AuditReportListingItem report : reports) {
            assertEquals(PENDING, report.getPipelineStatus());
            assertNull(report.getDownloadUrl());
        }
    }
}
