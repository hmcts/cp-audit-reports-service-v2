package uk.gov.hmcts.cp.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cp.controllers.base.SearchControllerBase;
import uk.gov.hmcts.cp.entities.User;
import uk.gov.hmcts.cp.mappers.UserMapper;
import uk.gov.hmcts.cp.openapi.api.UserSearchApi;
import uk.gov.hmcts.cp.openapi.model.GetUserIds200Response;
import uk.gov.hmcts.cp.openapi.model.UserSearchResult;
import uk.gov.hmcts.cp.services.UserSearchService;

@Slf4j
@RestController
public class UserSearchController
        extends SearchControllerBase<UserSearchService, User, UserSearchResult, GetUserIds200Response>
        implements UserSearchApi
{
    public UserSearchController(final UserSearchService service, final UserMapper mapper) {
        super(service, mapper, GetUserIds200Response::new);
    }

    @Override
    public ResponseEntity<GetUserIds200Response> getUserEmails(@NotNull @Valid final String userIds) {

        log.info("getUserEmails");
        return responseOk(service.getUsersByIds(userIds));
    }

    @Override
    public ResponseEntity<GetUserIds200Response> getUserIds(@NotNull @Valid final String emails) {

        log.info("getUserIds");
        return responseOk(service.getUsersByEmails(emails));
    }
}
