package uk.gov.hmcts.cp.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cp.entities.User;
import uk.gov.hmcts.cp.mappers.UserMapper;
import uk.gov.hmcts.cp.services.UserSearchService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cp.controllers.TestHelper.split;

@ExtendWith(MockitoExtension.class)
class UserSearchControllerTest {

    @Mock
    UserSearchService service;

    UserSearchController underTest;

    UserMapper mapper = Mappers.getMapper(UserMapper.class);

    List<User> users = List.of(
            new User("userId1", "firstName1", "lastName1", "email1"),
            new User("userId2", "firstName2", "lastName2", "email2"),
            new User("userId3", "firstName3", "lastName3", "email3")
    );

    @BeforeEach
    void setUp() {
        underTest = new UserSearchController(mapper, service);
    }

    @Test
    void test_getUserEmails() {

        // Given
        when(service.getUsersByIds(anyString())).thenAnswer(invocation -> {
            var userIds = split(invocation.getArgument(0));
            return users.stream().filter(user -> userIds.contains(user.userId())).toList();
        });

        // When
        var result = underTest.getUserEmails("userId1,userId2");

        // Then
        assertNotNull(result);
        assertNotNull(result.getBody());
        assertNotNull(result.getBody().getResults());
        assertEquals(2, result.getBody().getResults().size());

        assertNotNull(result.getBody().getResults().getFirst());
        assertEquals("email1", result.getBody().getResults().getFirst().getEmail());
        assertEquals("userId1", result.getBody().getResults().getFirst().getUserId());
        assertEquals("lastName1", result.getBody().getResults().getFirst().getLastName());
        assertEquals("firstName1", result.getBody().getResults().getFirst().getFirstName());

        assertNotNull(result.getBody().getResults().getLast());
        assertEquals("email2", result.getBody().getResults().getLast().getEmail());
        assertEquals("userId2", result.getBody().getResults().getLast().getUserId());
        assertEquals("lastName2", result.getBody().getResults().getLast().getLastName());
        assertEquals("firstName2", result.getBody().getResults().getLast().getFirstName());
    }

    @Test
    void test_getUserIds() {

        // Given
        when(service.getUsersByEmails(anyString())).thenAnswer(invocation -> {
            var emails = split(invocation.getArgument(0));
            return users.stream().filter(user -> emails.contains(user.email())).toList();
        });

        // When
        var result = underTest.getUserIds("email1,email2");

        // Then
        assertNotNull(result);
        assertNotNull(result.getBody());
        assertNotNull(result.getBody().getResults());
        assertEquals(2, result.getBody().getResults().size());

        assertNotNull(result.getBody().getResults().getFirst());
        assertEquals("email1", result.getBody().getResults().getFirst().getEmail());
        assertEquals("userId1", result.getBody().getResults().getFirst().getUserId());
        assertEquals("lastName1", result.getBody().getResults().getFirst().getLastName());
        assertEquals("firstName1", result.getBody().getResults().getFirst().getFirstName());

        assertNotNull(result.getBody().getResults().getLast());
        assertEquals("email2", result.getBody().getResults().getLast().getEmail());
        assertEquals("userId2", result.getBody().getResults().getLast().getUserId());
        assertEquals("lastName2", result.getBody().getResults().getLast().getLastName());
        assertEquals("firstName2", result.getBody().getResults().getLast().getFirstName());
    }
}