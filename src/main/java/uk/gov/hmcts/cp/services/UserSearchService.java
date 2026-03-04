package uk.gov.hmcts.cp.services;

import io.opentelemetry.api.internal.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.cp.entities.output.User;
import uk.gov.hmcts.cp.entities.output.Users;
import uk.gov.hmcts.cp.properties.ServiceProperties;
import uk.gov.hmcts.cp.utility.RecordUtils;

import java.util.List;

@Component
public record UserSearchService(
        RestClient restClient,
        ServiceProperties settings
) {
    public List<User> getUsersByIds(final String userIds) {

        return getUsers("userIds", userIds);
    }

    public List<User> getUsersByEmails(final String emails) {

        return getUsers("emails", emails);
    }

    private List<User> getUsers(final String filter, final String value) {

        return StringUtils.isNullOrEmpty(value) ?
                List.of() :
                RecordUtils.getRecords(
                        restClient, settings.users(), filter, value, Users.class
                ).users();
    }
}
