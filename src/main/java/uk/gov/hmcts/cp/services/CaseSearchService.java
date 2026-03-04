package uk.gov.hmcts.cp.services;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.cp.entities.Cases;
import uk.gov.hmcts.cp.entities.output.Case;
import uk.gov.hmcts.cp.properties.ServiceProperties;
import uk.gov.hmcts.cp.utility.RecordUtils;

import java.util.List;
import java.util.Map;

@Component
public record CaseSearchService(
        RestClient restClient,
        ServiceProperties settings
) {
    public List<Case> getCasesByIds(final String caseIds) {

        return getCases("targetIds", caseIds);
    }

    public List<Case> getCasesByUrns(final String caseUrns) {

        return getCases("sourceIds", caseUrns);
    }

    private List<Case> getCases(final String filter, final String value) {
        return RecordUtils.getRecordsWithParams(
                restClient,
                settings.cases(),
                Map.of(filter, value, "targetType", "CASE_ID"),
                Cases.class
        ).systemIds();
    }
}
