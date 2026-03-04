package uk.gov.hmcts.cp.properties;

import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.List;

public record AzureProperties(
        TokenType tokenType,
        List<String> scopes,
        @NestedConfigurationProperty FabricProperties fabric
) { }
