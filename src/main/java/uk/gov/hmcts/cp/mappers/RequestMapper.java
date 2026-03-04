package uk.gov.hmcts.cp.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.core.convert.converter.Converter;
import uk.gov.hmcts.cp.entities.input.ReportRequest;
import uk.gov.hmcts.cp.openapi.model.AuditReportRequest;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface RequestMapper extends Converter<AuditReportRequest, ReportRequest> {

    @Override
    @Mapping(target = "caseId", source = "caseUrn")
    @Mapping(target = "userId", source = "userEmail")
    @Mapping(target = "auditUserId", source = "userEmail")
    @Mapping(target = "auditUserEmail", source = "userEmail")
    @Mapping(target = "auditReference", expression = "java(java.lang.String.format(\"%s-%s\", java.time.Instant.now().toString().replace(\"-\", \"\").substring(0, 8), org.apache.commons.lang3.RandomStringUtils.secure().randomAlphanumeric(6)))")
    ReportRequest convert(AuditReportRequest auditReportRequest);
}
