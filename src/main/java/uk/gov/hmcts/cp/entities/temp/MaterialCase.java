package uk.gov.hmcts.cp.entities.temp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MaterialCase(String materialId, String courtDocumentId, String caseId, String caseUrn) {}
