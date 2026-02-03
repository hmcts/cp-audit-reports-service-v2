package uk.gov.hmcts.cp.utility;

import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.function.Function;

public interface ControllerHelper {

    static <T, R, E> ResponseEntity<E> responseOk(
            final List<T> list,
            final Function<T, R> toResult,
            final Function<List<R>, E> toWrapper
    ) {
        return ResponseEntity.ok(toWrapper.apply(list.stream().map(toResult).toList()));
    }
}
