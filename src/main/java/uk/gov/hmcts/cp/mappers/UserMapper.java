package uk.gov.hmcts.cp.mappers;

import org.mapstruct.Mapper;
import org.springframework.core.convert.converter.Converter;
import uk.gov.hmcts.cp.entities.User;
import uk.gov.hmcts.cp.openapi.model.UserSearchResult;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface UserMapper extends Converter<User, UserSearchResult> { }
