package uk.gov.hmcts.cp.entities.input;

import io.opentelemetry.api.internal.StringUtils;

import java.util.stream.Stream;

public record ReportRequest(
        String auditUserId,
        String auditUserEmail,
        String auditReference,
        String startDate,
        String endDate,
        boolean allUsers,
        String userId,
        String userEmail,
        String searchCriteria,
        String caseId,
        String caseUrn,
        String materialIds,
        String hearingId
) {

    public ReportRequest {

        if (Stream.of(auditUserId, auditUserEmail, auditReference, startDate, endDate).
                anyMatch(StringUtils::isNullOrEmpty) ||
                !allUsers && StringUtils.isNullOrEmpty(userEmail) ||
                switch (searchCriteria) {
                    case "CASE" -> StringUtils.isNullOrEmpty(caseUrn);
                    case "MATERIAL" -> StringUtils.isNullOrEmpty(materialIds);
                    case "HEARING" -> StringUtils.isNullOrEmpty(hearingId);
                    case "ALL_ACTIVITY" -> allUsers;
                    default -> true;
        }) {
            throw new IllegalArgumentException();
        }
    }
}
