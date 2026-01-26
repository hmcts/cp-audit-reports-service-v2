package uk.gov.hmcts.cp.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.cp.mappers.UserMapper;
import uk.gov.hmcts.cp.openapi.api.UserSearchApi;
import uk.gov.hmcts.cp.openapi.model.GetUserIds200Response;
import uk.gov.hmcts.cp.services.UserSearchService;

public record UserSearchController(
        UserSearchService service,
        UserMapper mapper
) implements UserSearchApi {

    @Override
    public ResponseEntity<GetUserIds200Response> getUserEmails(@NotNull @Valid String userIds) {

        // Map here to GetUserIds200Response
        // we are literally going to proxy these through. map them?

        return UserSearchApi.super.getUserEmails(userIds);
    }

    @Override
    public ResponseEntity<GetUserIds200Response> getUserIds(@NotNull @Valid String emails) {

        final var users = service.getUsersByEmails(emails, "correlation");

        return ResponseEntity.ok(GetUserIds200Response.builder().
                results(users.stream().map(mapper::mapUserToResponse).toList()).
                build()
        );
    }
}
