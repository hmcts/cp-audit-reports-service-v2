package uk.gov.hmcts.cp.services;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.cp.properties.ServiceProperties;

@Component
public record AuditReportsService(
        RestClient restClient,
        ServiceProperties settings
) {
}
