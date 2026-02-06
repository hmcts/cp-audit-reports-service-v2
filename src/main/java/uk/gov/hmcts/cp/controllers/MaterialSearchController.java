package uk.gov.hmcts.cp.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cp.controllers.base.SearchControllerBase;
import uk.gov.hmcts.cp.entities.Material;
import uk.gov.hmcts.cp.mappers.MaterialMapper;
import uk.gov.hmcts.cp.openapi.api.MaterialSearchApi;
import uk.gov.hmcts.cp.openapi.model.GetCaseIdsForMaterialIds200Response;
import uk.gov.hmcts.cp.openapi.model.MaterialSearchResult;
import uk.gov.hmcts.cp.services.MaterialSearchService;

@Slf4j
@RestController
public class MaterialSearchController
        extends SearchControllerBase<MaterialSearchService, Material, MaterialSearchResult, GetCaseIdsForMaterialIds200Response>
        implements MaterialSearchApi
{
    public MaterialSearchController(final MaterialSearchService service, final MaterialMapper mapper) {
        super(service, mapper, GetCaseIdsForMaterialIds200Response::new);
    }

    @Override
    public ResponseEntity<GetCaseIdsForMaterialIds200Response> getCaseIdsForMaterialIds(@NotNull @Valid final String materialIds) {

        log.info("getCaseIdsForMaterialIds");
        return responseOk(service.getMaterialCases(materialIds));
    }
}
