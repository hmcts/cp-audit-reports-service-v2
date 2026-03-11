package uk.gov.hmcts.cp.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cp.entities.output.Case;
import uk.gov.hmcts.cp.entities.output.Cases;
import uk.gov.hmcts.cp.properties.AzureProperties;
import uk.gov.hmcts.cp.properties.ClientProperties;
import uk.gov.hmcts.cp.properties.MediaProperties;
import uk.gov.hmcts.cp.properties.ServiceProperties;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.cp.properties.TokenType.TEST;

@ExtendWith(MockitoExtension.class)
class CaseSearchServiceTest extends SearchServiceTestBase<CaseSearchService> {

    Cases cases = new Cases(List.of(
            new Case("caseId1", "caseUrn1", "type1"),
            new Case("caseId2", "caseUrn2", "type2")
    ));

    @Override
    CaseSearchService createSearchService() {
        return new CaseSearchService(restClient, new ServiceProperties(
                "", "", new AzureProperties(TEST, List.of("test"), null), null, null,
                new ClientProperties("path", new MediaProperties("application", "json")), null));
    }

    @Test
    void test_getCasesByIds() {

        setUpStubs();

        // Given
        when(responseSpec.body(Cases.class)).thenReturn(cases);

        // When
        var result = underTest.getCasesByIds("caseId1,caseId2");

        // Then
        assertEquals("path?targetIds=caseId1,caseId2&targetType=CASE_ID", calledUri);
        assertSame(cases.systemIds(), result);
    }

    @Test
    void test_getCasesByUrns() {

        setUpStubs();

        // Given
        when(responseSpec.body(Cases.class)).thenReturn(cases);

        // When
        var result = underTest.getCasesByUrns("caseUrn1,caseUrn2");

        // Then
        assertEquals("path?targetType=CASE_ID&sourceIds=caseUrn1,caseUrn2", calledUri);
        assertSame(cases.systemIds(), result);
    }

    @Test
    void rest_client_not_called_for_null_getCasesByUrns_list() {

        // When
        underTest.getCasesByUrns(null);

        // Then
        verify(restClient, never()).get();
    }

    @Test
    void rest_client_not_called_for_empty_getCasesByUrns_list() {

        // When
        underTest.getCasesByUrns("");

        // Then
        verify(restClient, never()).get();
    }

    @Test
    void rest_client_not_called_for_null_getCasesByIds_list() {

        // When
        underTest.getCasesByIds(null);

        // Then
        verify(restClient, never()).get();
    }

    @Test
    void rest_client_not_called_for_empty_getCasesByIds_list() {

        // When
        underTest.getCasesByIds("");

        // Then
        verify(restClient, never()).get();
    }
}