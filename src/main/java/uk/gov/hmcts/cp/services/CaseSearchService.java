package uk.gov.hmcts.cp.services;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.cp.entities.Case;
import uk.gov.hmcts.cp.properties.ServiceProperties;
import uk.gov.hmcts.cp.utility.ClientHelper;

import java.util.List;

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

        return ClientHelper.getRecords(restClient, settings.cases(), filter, value);
    }
}
