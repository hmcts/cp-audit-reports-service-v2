package uk.gov.hmcts.cp.services;

import io.opentelemetry.api.internal.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.cp.entities.output.ReferralReason;
import uk.gov.hmcts.cp.entities.output.ReferralReasons;
import uk.gov.hmcts.cp.properties.ServiceProperties;
import uk.gov.hmcts.cp.utility.RecordUtils;

import java.util.List;

@Component
public record ReferenceDataSearchService(
        RestClient restClient,
        ServiceProperties settings
) {
    public List<ReferralReason> getReferralReasonsByIds(final String ids) {
        return getReferralReasons("ids", ids);
    }

    private List<ReferralReason> getReferralReasons(final String filter, final String value) {

        return StringUtils.isNullOrEmpty(value) ?
                List.of() :
                RecordUtils.getRecords(
                        restClient, settings.referralreasons(), filter, value, ReferralReasons.class
                ).referralReasons();
    }
}
