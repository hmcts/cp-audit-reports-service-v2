package uk.gov.hmcts.cp.entities.input;

public record ReportRequest(
        String auditUserId,
        String auditUserEmail,
        String auditReference,
        String startDate,
        String endDate,
        boolean allUsers,
        String userEmail,
        String userId,
        String searchCriteria,
        String caseUrn,
        String caseId,
        String materialIds,
        String hearingId
) {}
