package uk.gov.hmcts.cp.mappers;

import org.mapstruct.Mapper;
import uk.gov.hmcts.cp.entities.User;
import uk.gov.hmcts.cp.openapi.model.UserSearchResult;

@FunctionalInterface
@Mapper(componentModel = "spring")
public interface UserMapper {

    UserSearchResult mapUserToResult(User user);
}
