package uk.gov.hmcts.cp.utility;

import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public interface MapUtils {

    static UnaryOperator<Map<String, Object>> transformValues(final Map<String, UnaryOperator<Object>> transformers) {

        final Function<String, UnaryOperator<Object>> transformer = key ->
                transformers.getOrDefault(key, UnaryOperator.identity());

        return map -> map.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey, e -> transformer.apply(e.getKey()).apply(e.getValue())
        ));
    }

    static UnaryOperator<Object> toObjectUnaryOperator(final UnaryOperator<String> operator) {

        return obj -> operator.apply(obj.toString());
    }
}
