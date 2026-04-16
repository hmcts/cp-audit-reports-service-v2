package uk.gov.hmcts.cp.services;

import io.opentelemetry.api.internal.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.cp.entities.output.Case;
import uk.gov.hmcts.cp.entities.output.Cases;
import uk.gov.hmcts.cp.properties.ServiceProperties;
import uk.gov.hmcts.cp.utility.RecordUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

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

    private static final List<String> TARGET_TYPES = List.of(
            "CASE_FILE_ID",
            "caseId",
            "CASE-ID",
            "CASE_ID",
            "CPS_CASE_ID"
    );

    private List<Case> getCases(final String filter, final String value) {

        final Function<String, List<Case>> getTargetRecords = targetType -> RecordUtils.
                getRecordsWithParams(
                        restClient,
                        settings.cases(),
                        Map.of(filter, value, "targetType", targetType),
                        Cases.class
                ).systemIds();

        return StringUtils.isNullOrEmpty(value) ?
                List.of() :
                TARGET_TYPES.stream().map(getTargetRecords).flatMap(Collection::stream).distinct().toList();
    }
}
