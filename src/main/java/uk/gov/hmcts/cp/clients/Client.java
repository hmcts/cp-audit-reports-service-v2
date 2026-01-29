package uk.gov.hmcts.cp.clients;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.cp.properties.ServiceProperties;

@Configuration
public class Client {

    private static final String HEADER_USER = "CJSCPUID";

    @Bean
    public RestClient restClient(ServiceProperties settings) {

        return RestClient.builder().
                baseUrl(settings.baseUrl()).
                defaultHeader(HEADER_USER, settings.cjsCpUid()).
                build();
    }
}
