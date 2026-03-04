package uk.gov.hmcts.cp.http;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.cp.http.base.ApiTestBase;
import uk.gov.hmcts.cp.openapi.model.AuditReportRequest;
import uk.gov.hmcts.cp.openapi.model.PostReportRequest202Response;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.cp.openapi.model.AuditReportRequest.SearchCriteriaEnum.ALL_ACTIVITY;

class AuditReportsApiTest extends ApiTestBase<PostReportRequest202Response> {

    public AuditReportsApiTest() {
        super(PostReportRequest202Response.class);
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
        final ResponseEntity<PostReportRequest202Response> response = post("/reports", request, headers);

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
}
