package uk.gov.hmcts.cp.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.List;

@ConfigurationProperties("service")
public record ServiceProperties(
    String baseUrl,
    String cjsCpUid,
    List<String> scopes,
    TokenType tokenType,
    @NestedConfigurationProperty TableProperties table,
    @NestedConfigurationProperty ClientProperties users,
    @NestedConfigurationProperty ClientProperties cases,
    @NestedConfigurationProperty ClientProperties materials,
    @NestedConfigurationProperty FabricProperties fabric
) { }
