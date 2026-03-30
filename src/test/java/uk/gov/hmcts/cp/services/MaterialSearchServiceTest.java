package uk.gov.hmcts.cp.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cp.entities.output.Material;
import uk.gov.hmcts.cp.entities.output.Materials;
import uk.gov.hmcts.cp.properties.AzureProperties;
import uk.gov.hmcts.cp.properties.ClientProperties;
import uk.gov.hmcts.cp.properties.MediaProperties;
import uk.gov.hmcts.cp.properties.ServiceProperties;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cp.properties.CloudType.AZURE;
import static uk.gov.hmcts.cp.properties.TokenType.TEST;

@ExtendWith(MockitoExtension.class)
class MaterialSearchServiceTest extends SearchServiceTestBase<MaterialSearchService> {

    Materials materials = new Materials(List.of(
            new Material("materialId1", "documentId1", "caseId1", "caseUrn1"),
            new Material("materialId2", "documentId2", "caseId2", "caseUrn2")
    ));

    @Override
    MaterialSearchService createSearchService() {
        return new MaterialSearchService(restClient, new ServiceProperties(
                "", "", new AzureProperties(List.of("test"), TEST, AZURE, null, null, null), null, null,
                new ClientProperties("path", new MediaProperties("application", "json"))));
    }

    @Test
    void test_getMaterialCases() {

        setUpStubs();

        // Given
        when(responseSpec.body(Materials.class)).thenReturn(materials);

        // When
        var result = underTest.getMaterialCases("materialId1,materialId2");

        // Then
        assertEquals("path?materialIds=materialId1,materialId2", calledUri);
        assertSame(materials.materialIds(), result);
    }

    @Test
    void rest_client_not_called_for_null_getMaterialCases_list() {

        // When
        underTest.getMaterialCases(null);

        // Then
        verify(restClient, never()).get();
    }

    @Test
    void rest_client_not_called_for_empty_getMaterialCases_list() {

        // When
        underTest.getMaterialCases("");

        // Then
        verify(restClient, never()).get();
    }
}