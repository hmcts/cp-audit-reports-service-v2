package uk.gov.hmcts.cp.services;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.cp.entities.Case;
import uk.gov.hmcts.cp.properties.ServiceProperties;
import uk.gov.hmcts.cp.utility.ServiceHelper;

import java.util.List;
import java.util.Map;

@Component
public record CaseSearchService(
        RestClient restClient,
        ServiceProperties settings
) {
    public List<Case> getCasesByIds(final String caseIds) {

        return getCases("targetId", caseIds);
    }

    public List<Case> getCasesByUrns(final String caseUrns) {

        return getCases("sourceId", caseUrns);
    }

    private List<Case> getCases(final String filter, final String value) {

        return ServiceHelper.getRecordsWithParams(
                restClient,
                settings.cases(),
                Map.of(filter, value, "targetType", "CASE_ID"),
                new ParameterizedTypeReference<>() { }
        );
    }
}
