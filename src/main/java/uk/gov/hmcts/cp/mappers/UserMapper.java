package uk.gov.hmcts.cp.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import uk.gov.hmcts.cp.entities.User;
import uk.gov.hmcts.cp.openapi.model.UserSearchResult;

@FunctionalInterface
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(source = "id", target = "exampleId")
    UserSearchResult mapUserToResponse(User exampleEntity);
}
