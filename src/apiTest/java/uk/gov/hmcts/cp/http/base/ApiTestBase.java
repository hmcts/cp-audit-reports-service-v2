package uk.gov.hmcts.cp.http.base;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class ApiTestBase<T> {

    private final Class<T> responseType;

    protected final String baseUrl = System.getProperty("app.baseUrl", "http://localhost:8082");
    protected final RestTemplate http = new RestTemplate();

    protected ApiTestBase(final Class<T> responseType) {
        this.responseType = responseType;
    }

    protected ResponseEntity<T> get(final String url) {

        return http.exchange(baseUrl + url, HttpMethod.GET, null, responseType);
    }
}
