package uk.gov.hmcts.cp.services;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.cp.entities.Materials;
import uk.gov.hmcts.cp.entities.output.Material;
import uk.gov.hmcts.cp.properties.ServiceProperties;
import uk.gov.hmcts.cp.utility.RecordUtils;

import java.util.List;

@Component
public record MaterialSearchService(
        RestClient restClient,
        ServiceProperties settings
) {
    public List<Material> getMaterialCases(final String materialIds) {

        return RecordUtils.getRecords(
                restClient, settings.materials(), "materialIds", materialIds, Materials.class
        ).materialIds();
    }
}
