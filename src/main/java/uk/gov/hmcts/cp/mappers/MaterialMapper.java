package uk.gov.hmcts.cp.mappers;

import org.mapstruct.Mapper;
import org.springframework.core.convert.converter.Converter;
import uk.gov.hmcts.cp.entities.Material;
import uk.gov.hmcts.cp.openapi.model.MaterialSearchResult;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface MaterialMapper extends Converter<Material, MaterialSearchResult> { }
