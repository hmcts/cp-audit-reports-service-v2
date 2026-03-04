package uk.gov.hmcts.cp.entities.output;

public record Case(
        String targetId, // caseId
        String sourceId, // caseUrn
        String targetType
) {}