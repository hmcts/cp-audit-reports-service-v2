package uk.gov.hmcts.cp.properties;

public record FabricProperties(
    String baseUrl,
    String path,
    String itemId,
    String workspaceId
) { }
