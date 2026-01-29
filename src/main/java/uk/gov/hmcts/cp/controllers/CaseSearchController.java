package uk.gov.hmcts.cp.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Proxyable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cp.entities.Case;
import uk.gov.hmcts.cp.mappers.CaseMapper;
import uk.gov.hmcts.cp.openapi.api.CaseSearchApi;
import uk.gov.hmcts.cp.openapi.model.GetCaseUrns200Response;
import uk.gov.hmcts.cp.services.CaseSearchService;
import uk.gov.hmcts.cp.utility.ServiceHelper;

import java.util.List;

import static org.springframework.context.annotation.ProxyType.INTERFACES;

@Slf4j
@RestController
@Proxyable(INTERFACES)
public record CaseSearchController(
        CaseMapper mapper,
        CaseSearchService service
) implements CaseSearchApi {

    @Override
    public ResponseEntity<GetCaseUrns200Response> getCaseIds(@NotNull @Valid final String caseUrns) {

        log.info("getCaseIds");
        return responseOk(service.getCasesByUrns(caseUrns));
    }

    @Override
    public ResponseEntity<GetCaseUrns200Response> getCaseUrns(@NotNull @Valid final String caseIds) {

        log.info("getCaseUrns");
        return responseOk(service.getCasesByIds(caseIds));
    }

    private ResponseEntity<GetCaseUrns200Response> responseOk(final List<Case> cases) {

        return ServiceHelper.responseOk(cases, mapper::mapCaseToResult,
                results -> GetCaseUrns200Response.
                        builder().results(results).build());
    }
}
