package uk.gov.hmcts.cp.mappers;

import org.mapstruct.Mapper;
import uk.gov.hmcts.cp.entities.Material;
import uk.gov.hmcts.cp.openapi.model.MaterialSearchResult;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@FunctionalInterface
@Mapper(componentModel = SPRING)
public interface MaterialMapper {

    MaterialSearchResult mapMaterialToResult(Material material);
}
