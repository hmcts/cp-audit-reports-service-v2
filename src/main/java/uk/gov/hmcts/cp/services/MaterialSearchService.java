package uk.gov.hmcts.cp.services;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.cp.entities.Material;
import uk.gov.hmcts.cp.properties.ServiceProperties;
import uk.gov.hmcts.cp.utility.ServiceHelper;

import java.util.List;

@Component
public record MaterialSearchService(
        RestClient restClient,
        ServiceProperties settings
) {
    public List<Material> getMaterialCases(final String materialIds) {

        return ServiceHelper.getRecords(
                restClient, settings.material(), "materialIds", materialIds,
                new ParameterizedTypeReference<>() { }
        );
    }
}
