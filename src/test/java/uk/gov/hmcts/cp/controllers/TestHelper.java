package uk.gov.hmcts.cp.controllers;

import java.util.Arrays;
import java.util.List;

public interface TestHelper {

    static List<String> split(String text) {
        return Arrays.stream(text.split(",")).toList();
    }
}
