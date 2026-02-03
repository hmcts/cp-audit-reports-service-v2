package uk.gov.hmcts.cp.entities;

public record Case(
        String targetId, // caseId
        String sourceId, // caseUrn
        String targetType
) {}