package uk.gov.hmcts.cp.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cp.entities.output.ReferralReason;
import uk.gov.hmcts.cp.entities.output.ReferralReasons;
import uk.gov.hmcts.cp.properties.AzureProperties;
import uk.gov.hmcts.cp.properties.ClientProperties;
import uk.gov.hmcts.cp.properties.MediaProperties;
import uk.gov.hmcts.cp.properties.ServiceProperties;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cp.properties.CloudType.AZURE;
import static uk.gov.hmcts.cp.properties.TokenType.TEST;

@ExtendWith(MockitoExtension.class)
class ReferenceDataSearchServiceTest extends SearchServiceTestBase<ReferenceDataSearchService> {

    ReferralReasons referralReasons = new ReferralReasons(List.of(
            new ReferralReason("id", "seqId", "hearingCode", "reason", "reasonCode", "welshReason"),
            new ReferralReason("id2", "seqId2", "hearingCode2", "reason2", "reasonCode2", "welshReason2")
    ));

    @Override
    ReferenceDataSearchService createSearchService() {
        return new ReferenceDataSearchService(restClient, new ServiceProperties(
                "", "", new AzureProperties(List.of("test"), TEST, AZURE, null, null, null, 4, 10, null),
                null, null, null, new ClientProperties("path", new MediaProperties("application", "json"))));
    }

    @Test
    void test_getReferralReasonsByIds() {

        setUpStubs();

        // Given
        when(responseSpec.body(ReferralReasons.class)).thenReturn(referralReasons);

        // When
        var result = underTest.getReferralReasonsByIds("id,id2");

        // Then
        assertTrue(calledUris.contains("path?ids=id,id2"));
        assertSame(referralReasons.referralReasons(), result);
    }


    @Test
    void rest_client_not_called_for_null_getReferralReasonsByIds_list() {

        // When
        underTest.getReferralReasonsByIds(null);

        // Then
        verify(restClient, never()).get();
    }

    @Test
    void rest_client_not_called_for_empty_getReferralReasonsByIds_list() {

        // When
        underTest.getReferralReasonsByIds("");

        // Then
        verify(restClient, never()).get();
    }
}
