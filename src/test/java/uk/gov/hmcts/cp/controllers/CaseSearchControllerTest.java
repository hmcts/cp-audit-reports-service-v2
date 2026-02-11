package uk.gov.hmcts.cp.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cp.entities.Case;
import uk.gov.hmcts.cp.mappers.CaseMapper;
import uk.gov.hmcts.cp.services.CaseSearchService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cp.controllers.utility.SearchControllerTestHelper.filter;

@ExtendWith(MockitoExtension.class)
class CaseSearchControllerTest {

    @Mock
    CaseSearchService service;

    CaseSearchController underTest;

    CaseMapper mapper = Mappers.getMapper(CaseMapper.class);

    List<Case> cases = List.of(
            new Case("caseId1", "caseUrn1", "type1"),
            new Case("caseId2", "caseUrn2", "type2"),
            new Case("caseId3", "caseUrn3", "type3")
    );

    @BeforeEach
    void setUp() {
        underTest = new CaseSearchController(service, mapper);
    }

    @Test
    void test_getCaseIds() {

        // Given
        when(service.getCasesByUrns(anyString())).thenAnswer(filter(cases, Case::sourceId));

        // When
        var result = underTest.getCaseIds("caseUrn1,caseUrn2");

        // Then
        assertNotNull(result);
        assertNotNull(result.getBody());
        assertNotNull(result.getBody().getResults());
        assertEquals(2, result.getBody().getResults().size());

        assertNotNull(result.getBody().getResults().getFirst());
        assertEquals("caseId1", result.getBody().getResults().getFirst().getCaseId());
        assertEquals("caseUrn1", result.getBody().getResults().getFirst().getCaseUrn());
        assertEquals("type1", result.getBody().getResults().getFirst().getTargetType());

        assertNotNull(result.getBody().getResults().getLast());
        assertEquals("caseId2", result.getBody().getResults().getLast().getCaseId());
        assertEquals("caseUrn2", result.getBody().getResults().getLast().getCaseUrn());
        assertEquals("type2", result.getBody().getResults().getLast().getTargetType());
    }

    @Test
    void test_getCaseUrns() {

        // Given
        when(service.getCasesByIds(anyString())).thenAnswer(filter(cases, Case::targetId));

        // When
        var result = underTest.getCaseUrns("caseId1,caseId2");

        // Then
        assertNotNull(result);
        assertNotNull(result.getBody());
        assertNotNull(result.getBody().getResults());
        assertEquals(2, result.getBody().getResults().size());

        assertNotNull(result.getBody().getResults().getFirst());
        assertEquals("caseId1", result.getBody().getResults().getFirst().getCaseId());
        assertEquals("caseUrn1", result.getBody().getResults().getFirst().getCaseUrn());
        assertEquals("type1", result.getBody().getResults().getFirst().getTargetType());

        assertNotNull(result.getBody().getResults().getLast());
        assertEquals("caseId2", result.getBody().getResults().getLast().getCaseId());
        assertEquals("caseUrn2", result.getBody().getResults().getLast().getCaseUrn());
        assertEquals("type2", result.getBody().getResults().getLast().getTargetType());
    }
}