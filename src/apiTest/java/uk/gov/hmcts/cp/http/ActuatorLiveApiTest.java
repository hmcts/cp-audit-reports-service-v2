package uk.gov.hmcts.cp.http;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.cp.http.base.ApiTestBase;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

class ActuatorLiveApiTest extends ApiTestBase<String> {

    public ActuatorLiveApiTest() {
        super(String.class);
    }

    @Test
    void health_is_up() {

        // When
        final ResponseEntity<String> res = get("/actuator/health");

        // Then
        assertThat(res.getStatusCode()).isEqualTo(OK);
        assertThat(res.getBody()).contains("\"status\":\"UP\"");
    }

    @Disabled // Let's revisit this during our monitoring spike
    @Test
    void prometheus_is_exposed() {

        // When
        final ResponseEntity<String> res = http.exchange(
                baseUrl + "/actuator/prometheus", HttpMethod.GET,
                new HttpEntity<>(httpHeaders(Map.of(ACCEPT, TEXT_PLAIN_VALUE))),
                String.class
        );

        // Then
        assertThat(res.getStatusCode()).isEqualTo(OK);
        assertThat(res.getBody()).contains("application_started_time_seconds");
    }
}
