package uk.gov.hmcts.cp.utility;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public interface StreamUtils {

    static <T> Optional<T> last(final Stream<T> stream) {
        return stream.reduce((_, next) -> next);
    }

    static Function<String, Stream<String>> split(final String regex) {
        return text -> Arrays.stream(text.split(regex));
    }
}
