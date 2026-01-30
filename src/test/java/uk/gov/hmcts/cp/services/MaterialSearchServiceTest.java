package uk.gov.hmcts.cp.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import uk.gov.hmcts.cp.entities.Material;
import uk.gov.hmcts.cp.properties.ClientProperties;
import uk.gov.hmcts.cp.properties.MediaProperties;
import uk.gov.hmcts.cp.properties.ServiceProperties;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MaterialSearchServiceTest extends SearchServiceTestBase<MaterialSearchService> {

    List<Material> materials = List.of(
            new Material("materialId1", "documentId1", "caseId1", "caseUrn1"),
            new Material("materialId2", "documentId2", "caseId2", "caseUrn2")
    );

    @Override
    MaterialSearchService createSearchService() {
        return new MaterialSearchService(restClient, new ServiceProperties(
                "", "", null, null, new ClientProperties("path", new MediaProperties(
                "application", "json"))));
    }

    @Test
    void test_getMaterialCases() {

        // Given
        when(responseSpec.body(ArgumentMatchers.<ParameterizedTypeReference<List<Material>>>any()))
                .thenReturn(materials);

        // When
        var result = underTest.getMaterialCases("materialId1,materialId2");

        // Then
        assertEquals("path?materialIds=materialId1,materialId2&targetType=CASE_ID", calledUri);
        assertSame(materials, result);
    }
}