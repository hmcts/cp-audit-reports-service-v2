package uk.gov.hmcts.cp.entities;

public record Material(
        String materialId,
        String courtDocumentId,
        String caseId,
        String caseUrn
) {}
