package uk.gov.hmcts.cp.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cp.entities.Material;
import uk.gov.hmcts.cp.entities.User;
import uk.gov.hmcts.cp.mappers.MaterialMapper;
import uk.gov.hmcts.cp.mappers.UserMapper;
import uk.gov.hmcts.cp.services.MaterialSearchService;
import uk.gov.hmcts.cp.services.UserSearchService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cp.controllers.TestHelper.split;

@ExtendWith(MockitoExtension.class)
class MaterialSearchControllerTest {

    @Mock
    MaterialSearchService service;

    MaterialSearchController underTest;

    MaterialMapper mapper = Mappers.getMapper(MaterialMapper.class);

    List<Material> materials = List.of(
            new Material("materialId1", "documentId1", "caseId1", "caseUrn1"),
            new Material("materialId2", "documentId2", "caseId2", "caseUrn2"),
            new Material("materialId3", "documentId3", "caseId3", "caseUrn3")
    );

    @BeforeEach
    void setUp() {
        underTest = new MaterialSearchController(mapper, service);
    }

    @Test
    void test_getCaseIdsForMaterialIds() {

        // Given
        when(service.getMaterialCases(anyString())).thenAnswer(invocation -> {
            var materialIds = split(invocation.getArgument(0));
            return materials.stream().filter(aCase -> materialIds.contains(aCase.materialId())).toList();
        });

        // When
        var result = underTest.getCaseIdsForMaterialIds("materialId1,materialId2");

        // Then
        assertNotNull(result);
        assertNotNull(result.getBody());
        assertNotNull(result.getBody().getResults());
        assertEquals(2, result.getBody().getResults().size());

        assertNotNull(result.getBody().getResults().getFirst());
        assertEquals("caseId1", result.getBody().getResults().getFirst().getCaseId());
        assertEquals("caseUrn1", result.getBody().getResults().getFirst().getCaseUrn());
        assertEquals("materialId1", result.getBody().getResults().getFirst().getMaterialId());
        assertEquals("documentId1", result.getBody().getResults().getFirst().getCourtDocumentId());

        assertNotNull(result.getBody().getResults().getLast());
        assertEquals("caseId2", result.getBody().getResults().getLast().getCaseId());
        assertEquals("caseUrn2", result.getBody().getResults().getLast().getCaseUrn());
        assertEquals("materialId2", result.getBody().getResults().getLast().getMaterialId());
        assertEquals("documentId2", result.getBody().getResults().getLast().getCourtDocumentId());
    }
}