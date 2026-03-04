package uk.gov.hmcts.cp.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cp.mappers.ReportMapper;
import uk.gov.hmcts.cp.mappers.RequestMapper;
import uk.gov.hmcts.cp.mappers.ResultMapper;
import uk.gov.hmcts.cp.services.AuditReportsService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cp.utility.ReportUtils.createReport;

@ExtendWith(MockitoExtension.class)
public class AuditReportsControllerTest {

    @Mock
    AuditReportsService service;

    AuditReportsController underTest;

    ResultMapper resultMapper = Mappers.getMapper(ResultMapper.class);
    ReportMapper reportMapper = Mappers.getMapper(ReportMapper.class);
    RequestMapper requestMapper = Mappers.getMapper(RequestMapper.class);

    @BeforeEach
    void setUp() {
        underTest = new AuditReportsController(service, requestMapper, resultMapper, reportMapper);
    }

    @Test
    void test_getReportListing() {

        final var report = createReport();

        // Given
        when(service.getReports()).thenReturn(List.of(report));

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
}
