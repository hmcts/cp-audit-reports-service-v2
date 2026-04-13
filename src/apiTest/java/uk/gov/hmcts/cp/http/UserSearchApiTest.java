package uk.gov.hmcts.cp.http;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.cp.http.base.ApiTestBase;
import uk.gov.hmcts.cp.openapi.model.GetUserIds200Response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

class UserSearchApiTest extends ApiTestBase<GetUserIds200Response> {

    public UserSearchApiTest() {
        super(GetUserIds200Response.class);
    }

    @Test
    void users_endpoint_returns_user_for_userId() {

        // When
        final ResponseEntity<GetUserIds200Response> response = get("/user/id?userIds=00000000-0000-0000-0000-000000000123");

        // Then
        assertEquals(OK, response.getStatusCode());
        assertEquals(1, response.getBody().getResults().size());
        assertEquals("00000000-0000-0000-0000-000000000123", response.getBody().getResults().get(0).getUserId());
    }

    @Test
    void users_endpoint_returns_users_for_userIds() {

        // When
        final ResponseEntity<GetUserIds200Response> response = get("/user/id?userIds=234,345");

        // Then
        assertEquals(OK, response.getStatusCode());
        assertEquals(2, response.getBody().getResults().size());
        assertEquals("234", response.getBody().getResults().get(0).getUserId());
        assertEquals("345", response.getBody().getResults().get(1).getUserId());
    }

    @Test
    void users_endpoint_returns_400_error_for_missing_userIds() {

        // When
        final HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () ->
                get("/user/id"));

        // Then
        assertEquals(BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void users_endpoint_returns_user_for_email() {

        // When
        final ResponseEntity<GetUserIds200Response> response = get("/user/email?emails=no@where.com");

        // Then
        assertEquals(OK, response.getStatusCode());
        assertEquals(1, response.getBody().getResults().size());
        assertEquals("no@where.com", response.getBody().getResults().get(0).getEmail());
    }

    @Test
    void users_endpoint_returns_users_for_emails() {

        // When
        final ResponseEntity<GetUserIds200Response> response = get("/user/email?emails=billy@bob.com,jack@jones.com");

        // Then
        assertEquals(OK, response.getStatusCode());
        assertEquals(2, response.getBody().getResults().size());
        assertEquals("billy@bob.com", response.getBody().getResults().get(0).getEmail());
        assertEquals("jack@jones.com", response.getBody().getResults().get(1).getEmail());
    }

    @Test
    void users_endpoint_returns_400_error_for_missing_emails() {

        // When
        final HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () ->
                get("/user/email"));

        // Then
        assertEquals(BAD_REQUEST, exception.getStatusCode());
    }
}
