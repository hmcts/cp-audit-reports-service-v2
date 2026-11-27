package uk.gov.hmcts.cp.utility;

import java.util.regex.Pattern;

public final class HearingId {
    private static final Pattern CANONICAL_UUID = Pattern.compile(
            "[0-9a-fA-F]{8}(?:-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}");

    private HearingId() {
    }

    public static boolean isValid(final String value) {
        return value != null && CANONICAL_UUID.matcher(value).matches();
    }
}
