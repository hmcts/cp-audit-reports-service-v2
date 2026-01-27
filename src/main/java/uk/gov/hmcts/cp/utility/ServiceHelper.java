package uk.gov.hmcts.cp.utility;

import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.function.Function;

public interface ServiceHelper {

    static <T, R, E> ResponseEntity<E> responseOk(
            List<T> list,
            Function<T, R> toResult,
            Function<List<R>, E> toWrapper
    ) {
        return ResponseEntity.ok(toWrapper.apply(list.stream().map(toResult).toList()));
    }
}
