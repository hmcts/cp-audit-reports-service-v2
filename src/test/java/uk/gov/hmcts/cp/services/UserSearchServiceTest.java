package uk.gov.hmcts.cp.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import uk.gov.hmcts.cp.entities.User;
import uk.gov.hmcts.cp.properties.ClientProperties;
import uk.gov.hmcts.cp.properties.MediaProperties;
import uk.gov.hmcts.cp.properties.ServiceProperties;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserSearchServiceTest extends SearchServiceTestBase<UserSearchService> {

    List<User> users = List.of(
            new User("userId1", "firstName1", "lastName1", "email1"),
            new User("userId2", "firstName2", "lastName2", "email2")
    );

    @Override
    UserSearchService createSearchService() {
        return new UserSearchService(restClient, new ServiceProperties(
                "", "", new ClientProperties("path", new MediaProperties(
                "application", "json")), null, null));
    }

    @Test
    void test_getUsersByIds() {

        // Given
        when(responseSpec.body(ArgumentMatchers.<ParameterizedTypeReference<List<User>>>any()))
                .thenReturn(users);

        // When
        var result = underTest.getUsersByIds("userId1,userId2");

        // Then
        assertEquals("path?userIds=userId1,userId2", calledUri);
        assertSame(users, result);
    }

    @Test
    void test_getUsersByEmails() {

        // Given
        when(responseSpec.body(ArgumentMatchers.<ParameterizedTypeReference<List<User>>>any()))
                .thenReturn(users);

        // When
        var result = underTest.getUsersByEmails("email1,email2");

        // Then
        assertEquals("path?emails=email1,email2", calledUri);
        assertSame(users, result);
    }
}