package uk.gov.hmcts.cp.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import uk.gov.hmcts.cp.entities.Case;
import uk.gov.hmcts.cp.properties.ClientProperties;
import uk.gov.hmcts.cp.properties.MediaProperties;
import uk.gov.hmcts.cp.properties.ServiceProperties;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseSearchServiceTest extends SearchServiceTestBase<CaseSearchService> {

    List<Case> cases = List.of(
            new Case("caseId1", "caseUrn1", "type1"),
            new Case("caseId2", "caseUrn2", "type2")
    );

    @Override
    CaseSearchService createSearchService() {
        return new CaseSearchService(restClient, new ServiceProperties(
                "", "", null, new ClientProperties("path", new MediaProperties(
                "application", "json")), null));
    }

    @Test
    void test_getCasesByIds() {

        // Given
        when(responseSpec.body(ArgumentMatchers.<ParameterizedTypeReference<List<Case>>>any()))
                .thenReturn(cases);

        // When
        var result = underTest.getCasesByIds("caseId1,caseId2");

        // Then
        assertEquals("path?targetId=caseId1,caseId2&targetType=CASE_ID", calledUri);
        assertSame(cases, result);
    }

    @Test
    void test_getCasesByUrns() {

        // Given
        when(responseSpec.body(ArgumentMatchers.<ParameterizedTypeReference<List<Case>>>any()))
                .thenReturn(cases);

        // When
        var result = underTest.getCasesByUrns("caseUrn1,caseUrn2");

        // Then
        assertEquals("path?sourceId=caseUrn1,caseUrn2&targetType=CASE_ID", calledUri);
        assertSame(cases, result);
    }
}