package uk.gov.hmcts.cp.entities.output;

import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.function.Function;

public record Report(
        String auditUserId,
        String auditUserEmail,
        String auditReportReference,
        String startDate,
        String endDate,
        boolean allUsers,
        String userEmail,
        String userId,
        String searchCriteria,
        String caseUrn,
        String caseID,
        String materialIds,
        String hearingId,
        String pipelineJobId,
        String pipelineStatus,
        String pipelineStartTime,
        String pipelineEndTime,
        String pipelineStatusDescription
) {

    public static Function<Map<String, Object>, Report> fromMap(final ObjectMapper objectMapper) {
        return map -> objectMapper.convertValue(map, Report.class);
    }
}
