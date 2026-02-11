package uk.gov.hmcts.cp.mappers;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.cp.entities.Case;
import uk.gov.hmcts.cp.openapi.model.CaseSearchResult;

import static org.assertj.core.api.Assertions.assertThat;

public class CaseMapperTest {

    @Test
    void test_CaseMapper() {

        CaseMapper caseMapper = Mappers.getMapper(CaseMapper.class);

        // Given
        Case aCase = new Case("targetId", "sourceId", "targetType");

        // When
        CaseSearchResult result = caseMapper.convert(aCase);

        // Then
        assertThat(result.getCaseId()).isEqualTo(aCase.targetId());
        assertThat(result.getCaseUrn()).isEqualTo(aCase.sourceId());
        assertThat(result.getTargetType()).isEqualTo(aCase.targetType());
    }
}
