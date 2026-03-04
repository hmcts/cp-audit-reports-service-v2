package uk.gov.hmcts.cp.mappers;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.cp.entities.input.ReportRequest;
import uk.gov.hmcts.cp.openapi.model.AuditReportRequest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cp.openapi.model.AuditReportRequest.SearchCriteriaEnum.ALL_ACTIVITY;

public class RequestMapperTest {

    @Test
    void test_RequestMapper() {

        RequestMapper requestMapper = Mappers.getMapper(RequestMapper.class);

        // Given
        AuditReportRequest request = new AuditReportRequest(
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                true,
                "no@where.com",
                ALL_ACTIVITY,
                "abc",
                "123,456",
                "789"
        );

        // When
        ReportRequest result = requestMapper.convert(request);

        // Then
        assertThat(result.auditUserId()).isEqualTo(request.getUserEmail());
        assertThat(result.auditUserEmail()).isEqualTo(request.getUserEmail());
        assertThat(result.auditReference().length()).isEqualTo(15);
        assertThat(result.startDate()).isEqualTo(request.getStartDate().toString());
        assertThat(result.endDate()).isEqualTo(request.getEndDate().toString());
        assertThat(result.allUsers()).isEqualTo(request.getAllUsers());
        assertThat(result.userEmail()).isEqualTo(request.getUserEmail());
        assertThat(result.userId()).isEqualTo(request.getUserEmail());
        assertThat(result.searchCriteria()).isEqualTo("ALL_ACTIVITY");
        assertThat(result.caseUrn()).isEqualTo(request.getCaseUrn());
        assertThat(result.caseId()).isEqualTo(request.getCaseUrn());
        assertThat(result.materialIds()).isEqualTo(request.getMaterialIds());
        assertThat(result.hearingId()).isEqualTo(request.getHearingId());
    }
}
