package uk.gov.hmcts.cp.utility;

import uk.gov.hmcts.cp.entities.output.Report;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public interface ReportUtils {

    static Report createReport() {

        return new Report(
                UUID.randomUUID().toString(),
                "audit@example.com",
                "reference",
                LocalDate.now().minusDays(1).toString(),
                LocalDate.now().plusDays(1).toString(),
                false,
                "no@where.com",
                UUID.randomUUID().toString(),
                "ALL_ACTIVITY",
                "abc123",
                UUID.randomUUID().toString(),
                LocalDate.now().toString(),
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                "PENDING",
                Instant.now().minusSeconds(60).toString(),
                Instant.now().plusSeconds(60).toString(),
                "description"
        );
    }
}
