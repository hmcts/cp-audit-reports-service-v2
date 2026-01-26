package uk.gov.hmcts.cp.services;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.cp.entities.User;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
public record UserSearchService(RestClient restClient) {

    public List<User> getUsersByEmails(String emails, String correlationId) {

        //@TODO: correlation?, header user


        return restClient.get().
                uri("usersgroups-query-api/query/api/rest/usersgroups/usersemails?emails={emails}", emails).
                accept(APPLICATION_JSON).
                retrieve().body(new ParameterizedTypeReference<>() {});
    }
}
