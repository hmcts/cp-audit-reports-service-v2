package uk.gov.hmcts.cp.mappers;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.cp.entities.output.ReportResult;
import uk.gov.hmcts.cp.openapi.model.PostReportRequest202Response;

import static org.assertj.core.api.Assertions.assertThat;

public class ResultMapperTest {

    @Test
    void test_ResultMapper() {

        ResultMapper resultMapper = Mappers.getMapper(ResultMapper.class);

        // Given
        ReportResult result = new ReportResult("123", "abc");

        // When
        PostReportRequest202Response underTest = resultMapper.convert(result);

        // Then
        assertThat(underTest.getPipelineJobId()).isEqualTo(result.jobId());
        assertThat(underTest.getAuditReportReference()).isEqualTo(result.reference());
    }
}
