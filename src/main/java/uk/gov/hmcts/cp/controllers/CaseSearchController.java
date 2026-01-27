package uk.gov.hmcts.cp.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cp.entities.Case;
import uk.gov.hmcts.cp.mappers.CaseMapper;
import uk.gov.hmcts.cp.openapi.api.CaseSearchApi;
import uk.gov.hmcts.cp.openapi.model.GetCaseUrns200Response;
import uk.gov.hmcts.cp.services.CaseSearchService;
import uk.gov.hmcts.cp.utility.ServiceHelper;

import java.util.List;

@Slf4j
@RestController
public record CaseSearchController(
        CaseMapper mapper,
        CaseSearchService service

) implements CaseSearchApi {

    @Override
    public ResponseEntity<GetCaseUrns200Response> getCaseIds(@NotNull @Valid String caseUrns) {

        log.info("getCaseIds({})", caseUrns);
        return responseOk(service.geCasesByUrns(caseUrns));
    }

    @Override
    public ResponseEntity<GetCaseUrns200Response> getCaseUrns(@NotNull @Valid String caseIds) {

        log.info("getCaseUrns({})", caseIds);
        return responseOk(service.geCasesByIds(caseIds));
    }

    private ResponseEntity<GetCaseUrns200Response> responseOk(List<Case> cases) {

        return ServiceHelper.responseOk(cases, mapper::mapCaseToResult,
                results -> GetCaseUrns200Response.
                        builder().results(results).build());
    }
}
