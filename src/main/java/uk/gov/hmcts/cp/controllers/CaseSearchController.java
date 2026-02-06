package uk.gov.hmcts.cp.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cp.controllers.base.SearchControllerBase;
import uk.gov.hmcts.cp.entities.Case;
import uk.gov.hmcts.cp.mappers.CaseMapper;
import uk.gov.hmcts.cp.openapi.api.CaseSearchApi;
import uk.gov.hmcts.cp.openapi.model.CaseSearchResult;
import uk.gov.hmcts.cp.openapi.model.GetCaseUrns200Response;
import uk.gov.hmcts.cp.services.CaseSearchService;

@Slf4j
@RestController
public class CaseSearchController
        extends SearchControllerBase<CaseSearchService, Case, CaseSearchResult, GetCaseUrns200Response>
        implements CaseSearchApi
{
    public CaseSearchController(final CaseSearchService service, final CaseMapper mapper) {
        super(service, mapper, GetCaseUrns200Response::new);
    }

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
}
