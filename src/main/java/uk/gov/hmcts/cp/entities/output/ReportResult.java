package uk.gov.hmcts.cp.entities.output;

import java.util.function.Function;

public record ReportResult(
        String jobId,
        String reference
) {
    public static Function<String, ReportResult> toResult(final String reference) {
        return jobId -> new ReportResult(jobId, reference);
    }
}
