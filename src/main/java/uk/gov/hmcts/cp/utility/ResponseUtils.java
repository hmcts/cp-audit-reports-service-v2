package uk.gov.hmcts.cp.utility;

import org.springframework.http.ResponseEntity;

public interface ResponseUtils {

    static <T> ResponseEntity<T> responseInternalServerError() {
        return ResponseEntity.internalServerError().body(null);
    }

    static <T> ResponseEntity<T> responseBadRequest() {
        return ResponseEntity.badRequest().body(null);
    }
}
