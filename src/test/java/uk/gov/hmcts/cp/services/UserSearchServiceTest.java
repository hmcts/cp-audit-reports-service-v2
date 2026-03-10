package uk.gov.hmcts.cp.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cp.entities.output.User;
import uk.gov.hmcts.cp.entities.output.Users;
import uk.gov.hmcts.cp.properties.AzureProperties;
import uk.gov.hmcts.cp.properties.ClientProperties;
import uk.gov.hmcts.cp.properties.MediaProperties;
import uk.gov.hmcts.cp.properties.ServiceProperties;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cp.properties.TokenType.TEST;

@ExtendWith(MockitoExtension.class)
class UserSearchServiceTest extends SearchServiceTestBase<UserSearchService> {

    Users users = new Users(List.of(
            new User("userId1", "firstName1", "lastName1", "email1"),
            new User("userId2", "firstName2", "lastName2", "email2")
    ));

    @Override
    UserSearchService createSearchService() {
        return new UserSearchService(restClient, new ServiceProperties(
                "", "", new AzureProperties(TEST, List.of("test"), null),
                new ClientProperties("path", new MediaProperties("application", "json")), null, null));
    }

    @Test
    void test_getUsersByIds() {

        setUpStubs();

        // Given
        when(responseSpec.body(Users.class)).thenReturn(users);

        // When
        var result = underTest.getUsersByIds("userId1,userId2");

        // Then
        assertEquals("path?userIds=userId1,userId2", calledUri);
        assertSame(users.users(), result);
    }

    @Test
    void test_getUsersByEmails() {

        setUpStubs();

        // Given
        when(responseSpec.body(Users.class)).thenReturn(users);

        // When
        var result = underTest.getUsersByEmails("email1,email2");

        // Then
        assertEquals("path?emails=email1,email2", calledUri);
        assertSame(users.users(), result);
    }

    @Test
    void rest_client_not_called_for_null_getUsersByIds_list() {

        // When
        underTest.getUsersByIds(null);

        // Then
        verify(restClient, never()).get();
    }

    @Test
    void rest_client_not_called_for_empty_getUsersByIds_list() {

        // When
        underTest.getUsersByIds("");

        // Then
        verify(restClient, never()).get();
    }

    @Test
    void rest_client_not_called_for_null_getUsersByEmails_list() {

        // When
        underTest.getUsersByEmails(null);

        // Then
        verify(restClient, never()).get();
    }

    @Test
    void rest_client_not_called_for_empty_getUsersByEmails_list() {

        // When
        underTest.getUsersByEmails("");

        // Then
        verify(restClient, never()).get();
    }
}