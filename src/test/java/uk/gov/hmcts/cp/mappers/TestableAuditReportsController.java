package uk.gov.hmcts.cp.mappers;

import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.cp.controllers.AuditReportsController;
import uk.gov.hmcts.cp.services.AuditReportsService;
import uk.gov.hmcts.cp.services.CaseSearchService;
import uk.gov.hmcts.cp.services.UserSearchService;

import java.util.Optional;
import java.util.function.Function;

public class TestableAuditReportsController extends AuditReportsController {

    private final Function<String, Optional<String>> headers;

    public TestableAuditReportsController(
            AuditReportsService reportService,
            CaseSearchService caseService,
            UserSearchService userService,
            Function<String, Optional<String>> headers
    ) {
        final var resultMapper = Mappers.getMapper(ResultMapper.class);
        final var requestMapper = Mappers.getMapper(RequestMapper.class);

        requestMapper.caseService = caseService;    // TestableAuditReportsController must be in this package
        requestMapper.userService = userService;    // uk.gov.hmcts.cp.mappers to access the mapper fields

        this.headers = headers;

        super(reportService, userService, requestMapper, resultMapper);
    }

    @Override
    protected Optional<String> getHeader(String name) {
        return headers.apply(name);
    }
}
