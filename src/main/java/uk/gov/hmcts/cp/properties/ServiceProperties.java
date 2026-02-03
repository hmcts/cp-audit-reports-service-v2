package uk.gov.hmcts.cp.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties("service")
public record ServiceProperties(
    String baseUrl,
    String cjsCpUid,
    @NestedConfigurationProperty ClientProperties users,
    @NestedConfigurationProperty ClientProperties cases,
    @NestedConfigurationProperty ClientProperties mappings
) { }
