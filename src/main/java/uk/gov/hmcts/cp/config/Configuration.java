package uk.gov.hmcts.cp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.cp.properties.ServiceProperties;

@org.springframework.context.annotation.Configuration
public class Configuration {

    @Bean
    public RestClient restClient(ServiceProperties settings) {

        return RestClient.create(settings.baseUrl());
    }
}
