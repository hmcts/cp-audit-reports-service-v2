package uk.gov.hmcts.cp.mappers;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.cp.entities.Material;
import uk.gov.hmcts.cp.openapi.model.MaterialSearchResult;

import static org.assertj.core.api.Assertions.assertThat;

public class MaterialMapperTest {

    @Test
    void test_MaterialMapper() {

        MaterialMapper materialMapper = Mappers.getMapper(MaterialMapper.class);

        // Given
        Material material = new Material("a", "b", "c", "d");

        // When
        MaterialSearchResult result = materialMapper.convert(material);

        // Then
        assertThat(result.getCaseId()).isEqualTo(material.caseId());
        assertThat(result.getCaseUrn()).isEqualTo(material.caseUrn());
        assertThat(result.getMaterialId()).isEqualTo(material.materialId());
        assertThat(result.getCourtDocumentId()).isEqualTo(material.courtDocumentId());
    }
}
