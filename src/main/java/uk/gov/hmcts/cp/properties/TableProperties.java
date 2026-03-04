package uk.gov.hmcts.cp.properties;

public record TableProperties(
        String endpoint,
        String sasToken,
        String azureKey,
        String azureName
) { }
