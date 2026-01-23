package uk.gov.hmcts.cp.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.cp.openapi.api.CaseSearchApi;
import uk.gov.hmcts.cp.openapi.model.GetCaseUrns200Response;

public class CaseSearchController implements CaseSearchApi {

    @Override
    public ResponseEntity<GetCaseUrns200Response> getCaseIds(@NotNull @Valid String caseUrns) {
        return CaseSearchApi.super.getCaseIds(caseUrns);
    }

    @Override
    public ResponseEntity<GetCaseUrns200Response> getCaseUrns(@NotNull @Valid String caseIds) {
        return CaseSearchApi.super.getCaseUrns(caseIds);
    }
}
