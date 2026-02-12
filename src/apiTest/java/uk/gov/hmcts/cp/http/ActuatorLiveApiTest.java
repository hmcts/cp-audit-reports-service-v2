package uk.gov.hmcts.cp.http;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.cp.http.base.ApiTestBase;

import static org.assertj.core.api.Assertions.assertThat;

class ActuatorLiveApiTest extends ApiTestBase<String> {

    public ActuatorLiveApiTest() {
        super(String.class);
    }

    @Test
    void health_is_up() {

        // When
        final ResponseEntity<String> res = get("/actuator/health");

        // Then
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).contains("\"status\":\"UP\"");
    }

    @Disabled // Lets revisit this during our monitoring spike
    @Test
    void prometheus_is_exposed() {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(java.util.List.of(MediaType.TEXT_PLAIN));
        final ResponseEntity<String> res = http.exchange(
                baseUrl + "/actuator/prometheus", HttpMethod.GET,
                new HttpEntity<>(httpHeaders),
                String.class
        );
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).contains("application_started_time_seconds");
    }
}
