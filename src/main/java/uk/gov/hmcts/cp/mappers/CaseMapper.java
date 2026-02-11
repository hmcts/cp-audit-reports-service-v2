package uk.gov.hmcts.cp.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.core.convert.converter.Converter;
import uk.gov.hmcts.cp.entities.Case;
import uk.gov.hmcts.cp.openapi.model.CaseSearchResult;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface CaseMapper extends Converter<Case, CaseSearchResult> {

    @Override
    @Mapping(target = "caseId", source = "targetId")
    @Mapping(target = "caseUrn", source = "sourceId")
    CaseSearchResult convert(Case aCase);
}
