package uk.gov.hmcts.cp.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.core.convert.converter.Converter;
import uk.gov.hmcts.cp.entities.output.ReportResult;
import uk.gov.hmcts.cp.openapi.model.PostReportRequest202Response;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface ResultMapper extends Converter<ReportResult, PostReportRequest202Response> {

    @Override
    @Mapping(target = "pipelineJobId", source = "jobId")
    @Mapping(target = "auditReportReference", source = "reference")
    PostReportRequest202Response convert(ReportResult reportResult);
}
