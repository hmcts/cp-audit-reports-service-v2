package uk.gov.hmcts.cp.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.hmcts.cp.entities.input.ReportRequest;
import uk.gov.hmcts.cp.openapi.model.AuditReportRequest;
import org.mapstruct.Context;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.cp.services.CaseSearchService;
import uk.gov.hmcts.cp.services.UserSearchService;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public abstract class RequestMapper {

    @Autowired
    @SuppressWarnings("PMD")
    CaseSearchService caseService;

    @Autowired
    @SuppressWarnings("PMD")
    UserSearchService userService;

    @Mapping(target = "caseId", expression = "java(caseService.getCasesByUrns(request.getCaseUrn()).stream().findFirst().map(uk.gov.hmcts.cp.entities.output.Case::targetId).orElse(null))")
    @Mapping(target = "userId", expression = "java(userService.getUsersByEmails(request.getUserEmail()).stream().findFirst().map(uk.gov.hmcts.cp.entities.output.User::userId).orElse(null))")
    @Mapping(target = "auditUserId", expression = "java(cjsCppUid)")
    @Mapping(target = "auditUserEmail", expression = "java(userService.getUsersByIds(cjsCppUid).stream().findFirst().map(uk.gov.hmcts.cp.entities.output.User::email).orElse(null))")
    @Mapping(target = "auditReference", expression = "java(java.lang.String.format(\"%s-%s\", java.time.Instant.now().toString().replace(\"-\", \"\").substring(0, 8), org.apache.commons.lang3.RandomStringUtils.secure().randomAlphanumeric(6)))")
    public abstract ReportRequest convert(AuditReportRequest request, @Context String cjsCppUid);
}
