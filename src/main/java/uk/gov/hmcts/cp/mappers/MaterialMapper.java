package uk.gov.hmcts.cp.mappers;

import org.mapstruct.Mapper;
import uk.gov.hmcts.cp.entities.Material;
import uk.gov.hmcts.cp.openapi.model.MaterialSearchResult;

@FunctionalInterface
@Mapper(componentModel = "spring")
public interface MaterialMapper {

    MaterialSearchResult mapMaterialToResult(Material material);
}
