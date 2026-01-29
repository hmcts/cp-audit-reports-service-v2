package uk.gov.hmcts.cp.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Proxyable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cp.entities.User;
import uk.gov.hmcts.cp.mappers.UserMapper;
import uk.gov.hmcts.cp.openapi.api.UserSearchApi;
import uk.gov.hmcts.cp.openapi.model.GetUserIds200Response;
import uk.gov.hmcts.cp.services.UserSearchService;
import uk.gov.hmcts.cp.utility.ServiceHelper;

import java.util.List;

import static org.springframework.context.annotation.ProxyType.INTERFACES;

@Slf4j
@RestController
@Proxyable(INTERFACES)
public record UserSearchController(
        UserMapper mapper,
        UserSearchService service
) implements UserSearchApi {

    @Override
    public ResponseEntity<GetUserIds200Response> getUserEmails(@NotNull @Valid String userIds) {

        log.info("getUserEmails({})", userIds);
        return responseOk(service.getUsersByIds(userIds));
    }

    @Override
    public ResponseEntity<GetUserIds200Response> getUserIds(@NotNull @Valid String emails) {

        log.info("getUserIds({})", emails);
        return responseOk(service.getUsersByEmails(emails));
    }

    private ResponseEntity<GetUserIds200Response> responseOk(List<User> users) {

        return ServiceHelper.responseOk(users, mapper::mapUserToResult,
                results -> GetUserIds200Response.
                        builder().results(results).build());
    }
}
