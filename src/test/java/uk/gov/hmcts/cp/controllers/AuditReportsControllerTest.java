package uk.gov.hmcts.cp.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cp.services.AuditReportsService;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.cp.entities.output.Case;
import uk.gov.hmcts.cp.entities.output.User;
import uk.gov.hmcts.cp.mappers.TestableAuditReportsController;
import uk.gov.hmcts.cp.openapi.model.AuditReportRequest;
import uk.gov.hmcts.cp.openapi.model.PostReportRequest202Response;
import uk.gov.hmcts.cp.services.CaseSearchService;
import uk.gov.hmcts.cp.services.UserSearchService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.cp.entities.output.ReportResult.toResult;
import static uk.gov.hmcts.cp.utility.AuditReportRequestUtils.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.cp.utility.ReportUtils.createReport;

@ExtendWith(MockitoExtension.class)
public class AuditReportsControllerTest {

    @Mock
    AuditReportsService reportsService;

    @Mock
    CaseSearchService caseService;

    @Mock
    UserSearchService userService;

    AuditReportsController underTest;

    Map<String, String> headers;

    @BeforeEach
    public void setUp() {

        headers = new HashMap<>();
        underTest = new TestableAuditReportsController(
                reportsService, caseService, userService,
                name -> Optional.ofNullable(headers.get(name))
        );
    }

    @Test
    void test_getReportListing() {

        final var report = createReport();

        // Given
        when(reportsService.getReports()).thenReturn(List.of(report));

        // When
        var result = underTest.getReportListing();

        // Then
        assertNotNull(result);
        assertNotNull(result.getBody());
        assertNotNull(result.getBody().getReports());
        assertEquals(1, result.getBody().getReports().size());

        assertNotNull(result.getBody().getReports().getFirst());
        assertEquals(report.pipelineJobId(), result.getBody().getReports().getFirst().getPipelineJobId().toString());
    }

    @Test
    void test_valid_postReportRequest() {

        setUpStubs();
        when(reportsService.requestReport(any())).thenReturn(Optional.of(toResult("reference").apply("job")));

        // Given
        final AuditReportRequest request = createValidReportRequest();

        // When
        final ResponseEntity<PostReportRequest202Response> response = underTest.postReportRequest(request);

        // Then
        assertEquals(OK, response.getStatusCode());
        assertEquals("job", response.getBody().getPipelineJobId());
        assertEquals("reference", response.getBody().getAuditReportReference());
    }

    @Test
    void test_minimal_postReportRequest() {

        headers.put("CJSCPPUID", "audit");
        when(userService.getUsersByIds("audit")).thenReturn(List.of(
                new User("audit", "Audit", "Audit", "audit@example.com")));

        when(reportsService.requestReport(any())).thenReturn(Optional.of(toResult("reference").apply("job")));

        // Given
        final AuditReportRequest request = createMinimalReportRequest();

        // When
        final ResponseEntity<PostReportRequest202Response> response = underTest.postReportRequest(request);

        // Then
        assertEquals(OK, response.getStatusCode());
        assertEquals("job", response.getBody().getPipelineJobId());
        assertEquals("reference", response.getBody().getAuditReportReference());
    }

    @Test
    void test_invalid_postReportRequest() {

        setUpStubs();

        // Given
        final AuditReportRequest request = createInvalidReportRequest();

        // When
        final ResponseEntity<PostReportRequest202Response> response = underTest.postReportRequest(request);

        // Then
        assertEquals(BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void test_empty_lookup_postReportRequest_is_bad_request() {

        headers.put("CJSCPPUID", "audit");

        // Given
        final AuditReportRequest request = createValidReportRequest();

        // When
        final ResponseEntity<PostReportRequest202Response> response = underTest.postReportRequest(request);

        // Then
        assertEquals(BAD_REQUEST, response.getStatusCode());
    }

    private void setUpStubs() {

        Case aCase = new Case("234", "abc", "CASE_ID");
        User user = new User("jack", "Jack", "Jones", "jack@jones.com");
        User audit = new User("audit", "Audit", "Audit", "audit@example.com");

        when(caseService.getCasesByUrns(aCase.sourceId())).thenReturn(List.of(aCase));
        when(userService.getUsersByEmails(user.email())).thenReturn(List.of(user));
        when(userService.getUsersByIds(audit.userId())).thenReturn(List.of(audit));

        headers.put("CJSCPPUID", "audit");
    }
}
