package uk.gov.hmcts.cp.http.base;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ApiTestBase<T> {

    private final Class<T> responseType;

    protected final String baseUrl = System.getProperty("app.baseUrl", "http://localhost:8082");
    protected final RestTemplate http = new RestTemplate();

    protected ApiTestBase(final Class<T> responseType) {
        this.responseType = responseType;
    }

    protected ResponseEntity<T> get(final String url) {

        return get(url, httpHeaders(Map.of("CJSCPPUID", "00000000-0000-0000-0000-000000000000")));
    }

    protected <R, U> ResponseEntity<U> post(final String url, final R request, final HttpHeaders headers, final Class<U> postResponseType) {

        return http.exchange(baseUrl + url, HttpMethod.POST, new HttpEntity<>(request, headers), postResponseType);
    }

    protected ResponseEntity<T> get(final String url, final HttpHeaders headers) {

        return http.exchange(baseUrl + url, HttpMethod.GET, new HttpEntity<>(headers), responseType);
    }

    protected static HttpHeaders httpHeaders(final Map<String, String> headers) {

        return HttpHeaders.copyOf(CollectionUtils.toMultiValueMap(
                headers.entrySet().stream().collect(Collectors.toMap(
                        Map.Entry::getKey, entry -> List.of(entry.getValue())
                ))
        ));
    }

    protected <R> ResponseEntity<T> post(final String url, final R request, final HttpHeaders headers) {

        return http.exchange(baseUrl + url, HttpMethod.POST, new HttpEntity<>(request, headers), responseType);
    }
}
