package uk.gov.hmcts.cp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import uk.gov.hmcts.cp.properties.ServiceProperties;

@SpringBootApplication
@SuppressWarnings("HideUtilityClassConstructor")
@EnableConfigurationProperties(ServiceProperties.class)
public class Application {

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}