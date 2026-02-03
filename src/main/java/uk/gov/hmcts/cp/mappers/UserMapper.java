package uk.gov.hmcts.cp.mappers;

import org.mapstruct.Mapper;
import uk.gov.hmcts.cp.entities.User;
import uk.gov.hmcts.cp.openapi.model.UserSearchResult;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@FunctionalInterface
@Mapper(componentModel = SPRING)
public interface UserMapper {

    UserSearchResult mapUserToResult(User user);
}
