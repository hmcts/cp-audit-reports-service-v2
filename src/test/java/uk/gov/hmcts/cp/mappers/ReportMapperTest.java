package uk.gov.hmcts.cp.mappers;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.cp.entities.output.Report;
import uk.gov.hmcts.cp.openapi.model.AuditReportListingItem;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cp.utility.ReportUtils.createReport;

public class ReportMapperTest {

    @Test
    void test_ReportMapper() {

        ReportMapper reportMapper = Mappers.getMapper(ReportMapper.class);

        // Given
        Report report = createReport();

        // When
        AuditReportListingItem result =  reportMapper.convert(report);

        // Then
        assertThat(result.getAuditReportReference()).isEqualTo(report.auditReportReference());
        assertThat(result.getAuditUserId().toString()).isEqualTo(report.auditUserId());
        assertThat(result.getAuditUserEmail()).isEqualTo(report.auditUserEmail());
        assertThat(result.getAuditReportReference()).isEqualTo(report.auditReportReference());
        assertThat(result.getStartDate()).isEqualTo(report.startDate());
        assertThat(result.getEndDate()).isEqualTo(report.endDate());
        assertThat(result.getAllUsers()).isEqualTo(report.allUsers());
        assertThat(result.getUserId().toString()).isEqualTo(report.userId());
        assertThat(result.getSearchCriteria().toString()).isEqualTo("all-activity");
        assertThat(result.getCaseUrn()).isEqualTo(report.caseUrn());
        assertThat(result.getCaseID().toString()).isEqualTo(report.caseID());
        assertThat(result.getMaterialIds().toString()).isEqualTo(report.materialIds());
        assertThat(result.getHearingId().toString()).isEqualTo(report.hearingId());
        assertThat(result.getPipelineJobId().toString()).isEqualTo(report.pipelineJobId());
        assertThat(result.getPipelineStatus().toString()).isEqualTo(report.pipelineStatus());
        assertThat(result.getPipelineStartTime().toString()).isEqualTo(report.pipelineStartTime());
        assertThat(result.getPipelineEndTime().toString()).isEqualTo(report.pipelineEndTime());
        assertThat(result.getPipelineStatusDescription()).isEqualTo(report.pipelineStatusDescription());
    }
}
