package uk.gov.hmcts.cp.entities.output;

public record Material(
        String materialId,
        String courtDocumentId,
        String caseId,
        String caseUrn
) {}
