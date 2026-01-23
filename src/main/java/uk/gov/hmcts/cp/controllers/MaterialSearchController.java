package uk.gov.hmcts.cp.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.cp.openapi.api.MaterialSearchApi;
import uk.gov.hmcts.cp.openapi.model.GetCaseIdsForMaterialIds200Response;

public class MaterialSearchController implements MaterialSearchApi {

    @Override
    public ResponseEntity<GetCaseIdsForMaterialIds200Response> getCaseIdsForMaterialIds(@NotNull @Valid String materialIds) {
        return MaterialSearchApi.super.getCaseIdsForMaterialIds(materialIds);
    }
}
