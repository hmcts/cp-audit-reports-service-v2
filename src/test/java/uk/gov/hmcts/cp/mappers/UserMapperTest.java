package uk.gov.hmcts.cp.mappers;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.cp.entities.User;
import uk.gov.hmcts.cp.openapi.model.UserSearchResult;

import static org.assertj.core.api.Assertions.assertThat;

public class UserMapperTest {

    @Test
    void test_UserMapper() {

        UserMapper userMapper = Mappers.getMapper(UserMapper.class);

        // Given
        User user = new User("`123", "John", "Smith", "no@where.com");

        // When
        UserSearchResult result = userMapper.convert(user);

        // Then
        assertThat(result.getEmail()).isEqualTo(user.email());
        assertThat(result.getUserId()).isEqualTo(user.userId());
        assertThat(result.getFirstName()).isEqualTo(user.firstName());
        assertThat(result.getLastName()).isEqualTo(user.lastName());
    }
}
