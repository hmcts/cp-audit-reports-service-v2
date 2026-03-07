package uk.gov.hmcts.cp.http;

import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.cp.http.base.ApiTestBase;

import java.util.Map;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthorisationApiTest extends ApiTestBase<String> {

    private static final AuthorisationApiTest INSTANCE = new AuthorisationApiTest();

    private enum Verb {

        GET(INSTANCE::get),
        POST((url, headers) -> INSTANCE.post(url, null, headers));

        private final BiFunction<String, HttpHeaders, ResponseEntity<String>> action;

        Verb(final BiFunction<String, HttpHeaders, ResponseEntity<String>> action) {
            this.action = action;
        }

        public BiFunction<String, HttpHeaders, ResponseEntity<String>> getAction() {
            return action;
        }
    }

    public AuthorisationApiTest() {
        super(String.class);
    }

    @ParameterizedTest
    @CsvSource({
            "true,GET,/case/id?caseIds=123,alice",
            "false,GET,/case/id?caseIds=123,chris",
            "true,GET,/case/id?caseIds=123,fabric"
    })
    void test_endpoint_user_authorisation(
            final boolean shouldHaveAccess, final Verb verb, final String url, final String userId
    ) throws Throwable {

        final Executable invokeUrl = () -> verb.getAction().apply(
                url, httpHeaders(Map.of("CJSCPPUID", userId)));

        if (shouldHaveAccess) {
            invokeUrl.execute();
        } else {
            assertThrows(HttpClientErrorException.Forbidden.class, invokeUrl);
        }
    }
}
