package uk.gov.hmcts.cp.controllers.entities.input;

import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.hmcts.cp.entities.input.ReportRequest;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ReportRequestTest {

    @ParameterizedTest
    @CsvSource({
            "true,audit123,audit@example.com,auditReference,2024-02-01,2024-04-02,false,,john@smith.com,ALL_ACTIVITY,,,,",
            "false,,audit@example.com,auditReference,2024-02-01,2024-04-02,false,,,ALL_ACTIVITY,,,,",
            "false,audit123,,auditReference,2024-02-01,2024-04-02,false,,,ALL_ACTIVITY,,,,",
            "false,audit123,audit@example.com,,2024-02-01,2024-04-02,false,,,ALL_ACTIVITY,,,,",
            "false,audit123,audit@example.com,auditReference,,2024-04-02,false,,,ALL_ACTIVITY,,,,",
            "false,audit123,audit@example.com,auditReference,2024-02-01,,false,,,ALL_ACTIVITY,,,,",
            "false,audit123,audit@example.com,auditReference,2024-02-01,2024-04-02,true,john,john@smith.com,ALL_ACTIVITY,,,,",
            "false,audit123,audit@example.com,auditReference,2024-02-01,2024-04-02,false,,,BAD_ENUM,,,,",
            "true,audit123,audit@example.com,auditReference,2024-02-01,2024-04-02,true,john,john@smith.com,CASE,123,abc,,",
            "false,audit123,audit@example.com,auditReference,2024-02-01,2024-04-02,false,john,,CASE,123,abc,,",
            "false,audit123,audit@example.com,auditReference,2024-02-01,2024-04-02,true,john,john@smith.com,CASE,,,,",
            "true,audit123,audit@example.com,auditReference,2024-02-01,2024-04-02,true,john,john@smith.com,MATERIAL,,,678,",
            "false,audit123,audit@example.com,auditReference,2024-02-01,2024-04-02,true,john,john@smith.com,MATERIAL,,,,",
            "true,audit123,audit@example.com,auditReference,2024-02-01,2024-04-02,true,john,john@smith.com,HEARING,,,,xyz",
            "false,audit123,audit@example.com,auditReference,2024-02-01,2024-04-02,true,john,john@smith.com,HEARING,,,,",
    })
    void validateReportRequest(
            boolean shouldBeValid,
            String auditUserId,
            String auditUserEmail,
            String auditReference,
            String startDate,
            String endDate,
            boolean allUsers,
            String userId,
            String userEmail,
            String searchCriteria,
            String caseId,
            String caseUrn,
            String materialIds,
            String hearingId
    ) throws Throwable {

        final Executable createReportRequest = () -> new ReportRequest(
                auditUserId, auditUserEmail, auditReference,
                startDate, endDate, allUsers,
                userId, userEmail, searchCriteria,
                caseId, caseUrn, materialIds, hearingId
        );

        if (shouldBeValid) {
            createReportRequest.execute();
        } else {
            assertThrows(IllegalArgumentException.class, createReportRequest);
        }
    }
}
