package uk.gov.hmcts.cp.http;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.cp.http.base.LiveTestBase;
import uk.gov.hmcts.cp.openapi.model.GetCaseUrns200Response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CaseSearchLiveTest extends LiveTestBase<GetCaseUrns200Response> {

    public CaseSearchLiveTest() {
        super(GetCaseUrns200Response.class);
    }

    @Test
    void cases_endpoint_returns_case_for_caseId() {

        // When
        final ResponseEntity<GetCaseUrns200Response> response = get("/case/id?caseIds=123");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getResults().size());
        assertEquals("123", response.getBody().getResults().get(0).getCaseId());
    }

    @Test
    void cases_endpoint_returns_cases_for_caseIds() {

        // When
        final ResponseEntity<GetCaseUrns200Response> response = get("/case/id?caseIds=234,345");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().getResults().size());
        assertEquals("234", response.getBody().getResults().get(0).getCaseId());
        assertEquals("345", response.getBody().getResults().get(1).getCaseId());
    }

    @Test
    void cases_endpoint_returns_400_error_for_missing_caseIds() {

        // When
        final HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () ->
                get("/case/id"));

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void cases_endpoint_returns_case_for_caseUrn() {

        // When
        final ResponseEntity<GetCaseUrns200Response> response = get("/case/urn?caseUrns=abc");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getResults().size());
        assertEquals("abc", response.getBody().getResults().get(0).getCaseUrn());
    }

    @Test
    void cases_endpoint_returns_cases_for_caseUrns() {

        // When
        final ResponseEntity<GetCaseUrns200Response> response = get("/case/urn?caseUrns=bcd,cde");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().getResults().size());
        assertEquals("bcd", response.getBody().getResults().get(0).getCaseUrn());
        assertEquals("cde", response.getBody().getResults().get(1).getCaseUrn());
    }

    @Test
    void cases_endpoint_returns_400_error_for_missing_caseUrns() {

        // When
        final HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () ->
                get("/case/urn"));

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }
}
