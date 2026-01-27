package uk.gov.hmcts.cp.properties;

import org.springframework.boot.context.properties.NestedConfigurationProperty;

public record ClientProperties(
        String path,
        @NestedConfigurationProperty MediaProperties media
) { }
