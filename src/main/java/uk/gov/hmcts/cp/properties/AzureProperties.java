package uk.gov.hmcts.cp.properties;

import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.List;

public record AzureProperties(
        List<String> scopes,
        TokenType tokenType,
        CloudType cloudType,
        String tableName,
        String tableEndpoint,
        @NestedConfigurationProperty FabricProperties fabric
) { }
