package uk.gov.hmcts.cp.utility;

import uk.gov.hmcts.cp.openapi.model.AuditReportRequest;

import java.time.LocalDate;

import static uk.gov.hmcts.cp.openapi.model.AuditReportRequest.SearchCriteriaEnum.ALL_ACTIVITY;
import static uk.gov.hmcts.cp.openapi.model.AuditReportRequest.SearchCriteriaEnum.HEARING;

public interface AuditReportRequestUtils {

    static AuditReportRequest createValidReportRequest() {
        return new AuditReportRequest(
                LocalDate.now().minusDays(2),
                LocalDate.now().minusDays(1),
                false,
                "jack@jones.com",
                ALL_ACTIVITY,
                "abc",
                "123,456",
                "789"
        );
    }

    static AuditReportRequest createMinimalReportRequest() {
        return new AuditReportRequest(
                LocalDate.now().minusDays(2),
                LocalDate.now().minusDays(1),
                true,
                null,
                HEARING,
                null,
                null,
                "789"
        );
    }

    static AuditReportRequest createInvalidReportRequest() {
        return new AuditReportRequest(
                LocalDate.now().minusDays(2),
                LocalDate.now().minusDays(1),
                true,
                "jack@jones.com",
                ALL_ACTIVITY,
                "abc",
                "123,456",
                "789"
        );
    }
}
