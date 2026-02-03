package uk.gov.hmcts.cp.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Proxyable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cp.entities.Material;
import uk.gov.hmcts.cp.mappers.MaterialMapper;
import uk.gov.hmcts.cp.openapi.api.MaterialSearchApi;
import uk.gov.hmcts.cp.openapi.model.GetCaseIdsForMaterialIds200Response;
import uk.gov.hmcts.cp.services.MaterialSearchService;
import uk.gov.hmcts.cp.utility.ControllerHelper;

import java.util.List;

import static org.springframework.context.annotation.ProxyType.INTERFACES;

@Slf4j
@RestController
@Proxyable(INTERFACES)
public record MaterialSearchController(
        MaterialMapper mapper,
        MaterialSearchService service
) implements MaterialSearchApi {

    @Override
    public ResponseEntity<GetCaseIdsForMaterialIds200Response> getCaseIdsForMaterialIds(@NotNull @Valid final String materialIds) {

        log.info("getCaseIdsForMaterialIds");
        return responseOk(service.getMaterialCases(materialIds));
    }

    private ResponseEntity<GetCaseIdsForMaterialIds200Response> responseOk(final List<Material> cases) {

        return ControllerHelper.responseOk(cases, mapper::mapMaterialToResult,
                results -> GetCaseIdsForMaterialIds200Response.
                        builder().results(results).build());
    }
}
