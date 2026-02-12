package uk.gov.hmcts.cp.controllers.utility;

import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public interface SearchControllerTestHelper {

    static <T> Answer<List<T>> filter(List<T> list, Function<T, String> getKey) {
        return invocation -> {
            var ids = split(invocation.getArgument(0));
            return list.stream().filter(item -> ids.contains(getKey.apply(item))).toList();
        };
    }

    static List<String> split(String text) {
        return Arrays.stream(text.split(",")).toList();
    }
}
