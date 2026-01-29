package uk.gov.hmcts.cp.services;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.cp.entities.Material;
import uk.gov.hmcts.cp.properties.ServiceProperties;
import uk.gov.hmcts.cp.utility.ClientHelper;

import java.util.List;
import java.util.Map;

@Component
public record MaterialSearchService(
        RestClient restClient,
        ServiceProperties settings
) {
    public List<Material> getMaterialCases(String materialIds) {

        return ClientHelper.getRecordsWithParams(restClient, settings.mappings(),
                Map.of("materialIds", materialIds, "targetType", "CASE_ID"));
    }
}
