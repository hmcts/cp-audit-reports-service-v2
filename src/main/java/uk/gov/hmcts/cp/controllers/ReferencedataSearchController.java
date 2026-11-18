package uk.gov.hmcts.cp.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cp.controllers.base.SearchControllerBase;
import uk.gov.hmcts.cp.entities.output.ReferralReason;
import uk.gov.hmcts.cp.mappers.ReferralReasonMapper;
import uk.gov.hmcts.cp.openapi.api.ReferencedataSearchApi;
import uk.gov.hmcts.cp.openapi.model.GetReferralReasons200Response;
import uk.gov.hmcts.cp.openapi.model.ReferenceDataSearchResult;
import uk.gov.hmcts.cp.services.ReferenceDataSearchService;

@Slf4j
@RestController
public class ReferencedataSearchController extends SearchControllerBase<ReferenceDataSearchService, ReferralReason, ReferenceDataSearchResult, GetReferralReasons200Response>
        implements ReferencedataSearchApi {
    public ReferencedataSearchController(final ReferenceDataSearchService service, final ReferralReasonMapper mapper) {
        super(service, mapper, GetReferralReasons200Response::new);
    }

    @Override
    public ResponseEntity<GetReferralReasons200Response> getReferralReasons(@NotNull @Valid final String ids) {

        log.info("getReferralReasons");
        return responseOk(service.getReferralReasonsByIds(ids));
    }
}
