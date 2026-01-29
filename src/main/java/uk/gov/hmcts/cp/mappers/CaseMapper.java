package uk.gov.hmcts.cp.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.hmcts.cp.entities.Case;
import uk.gov.hmcts.cp.openapi.model.CaseSearchResult;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@FunctionalInterface
@Mapper(componentModel = SPRING)
public interface CaseMapper {

    @Mapping(target = "caseId", source = "targetId")
    @Mapping(target = "caseUrn", source = "sourceId")
    CaseSearchResult mapCaseToResult(Case aCase);
}
