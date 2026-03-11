package uk.gov.hmcts.cp.mappers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cp.entities.input.ReportRequest;
import uk.gov.hmcts.cp.entities.output.Case;
import uk.gov.hmcts.cp.entities.output.User;
import uk.gov.hmcts.cp.openapi.model.AuditReportRequest;
import uk.gov.hmcts.cp.services.CaseSearchService;
import uk.gov.hmcts.cp.services.UserSearchService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cp.utility.AuditReportRequestUtils.createValidReportRequest;

@ExtendWith(MockitoExtension.class)
public class RequestMapperTest {

    @Mock
    CaseSearchService caseService;

    @Mock
    UserSearchService userService;

    @Test
    void test_RequestMapper() {

        RequestMapper requestMapper = Mappers.getMapper(RequestMapper.class);

        requestMapper.caseService = caseService;
        requestMapper.userService = userService;

        Case aCase = new Case("234", "abc", "CASE_ID");
        User user = new User("jack", "Jack", "Jones", "jack@jones.com");
        User audit = new User("audit", "Audit", "Audit", "audit@example.com");

        // Given
        AuditReportRequest request = createValidReportRequest();

        when(caseService.getCasesByUrns(aCase.sourceId())).thenReturn(List.of(aCase));
        when(userService.getUsersByEmails(user.email())).thenReturn(List.of(user));
        when(userService.getUsersByIds(audit.userId())).thenReturn(List.of(audit));

        // When
        ReportRequest result = requestMapper.convert(request, "audit");

        // Then
        assertThat(result.auditUserId()).isEqualTo(audit.userId());
        assertThat(result.auditUserEmail()).isEqualTo(audit.email());
        assertThat(result.auditReference().length()).isEqualTo(15);

        assertThat(result.startDate()).isEqualTo(request.getStartDate().toString());
        assertThat(result.endDate()).isEqualTo(request.getEndDate().toString());
        assertThat(result.allUsers()).isEqualTo(request.getAllUsers());
        assertThat(result.userEmail()).isEqualTo(request.getUserEmail());

        assertThat(result.userId()).isEqualTo(user.userId());
        assertThat(result.searchCriteria()).isEqualTo("ALL_ACTIVITY");
        assertThat(result.caseUrn()).isEqualTo(request.getCaseUrn());
        assertThat(result.caseId()).isEqualTo(aCase.targetId());

        assertThat(result.materialIds()).isEqualTo(request.getMaterialIds());
        assertThat(result.hearingId()).isEqualTo(request.getHearingId());
    }
}
