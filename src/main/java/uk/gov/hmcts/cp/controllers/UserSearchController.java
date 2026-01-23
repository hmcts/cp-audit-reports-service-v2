package uk.gov.hmcts.cp.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.cp.openapi.api.UserSearchApi;
import uk.gov.hmcts.cp.openapi.model.GetUserIds200Response;

public class UserSearchController implements UserSearchApi {

    @Override
    public ResponseEntity<GetUserIds200Response> getUserEmails(@NotNull @Valid String userIds) {
        return UserSearchApi.super.getUserEmails(userIds);
    }

    @Override
    public ResponseEntity<GetUserIds200Response> getUserIds(@NotNull @Valid String emails) {
        return UserSearchApi.super.getUserIds(emails);
    }
}
