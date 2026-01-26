package uk.gov.hmcts.cp.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("service")
public record ServiceProperties(
    String baseUrl
) { }
