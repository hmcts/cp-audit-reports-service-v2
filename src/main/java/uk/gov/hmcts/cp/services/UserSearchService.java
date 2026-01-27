package uk.gov.hmcts.cp.services;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.cp.entities.User;
import uk.gov.hmcts.cp.properties.ServiceProperties;
import uk.gov.hmcts.cp.utility.ClientHelper;

import java.util.List;

@Component
public record UserSearchService(
        RestClient restClient,
        ServiceProperties settings
) {
    public List<User> getUsersByIds(String userIds) {

        return getUsers("userIds", userIds);
    }

    public List<User> getUsersByEmails(String emails) {

        return getUsers("emails", emails);
    }

    private List<User> getUsers(String filter, String value) {

        return ClientHelper.getRecords(restClient, settings.users(), filter, value);
    }
}
