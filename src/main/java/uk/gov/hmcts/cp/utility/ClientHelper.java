package uk.gov.hmcts.cp.utility;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.cp.properties.ClientProperties;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface ClientHelper {

    String HEADER_CORRELATION_ID = "CPCLIENTCORRELATIONID";

    static <T> List<T> getRecords(
            RestClient restClient,
            ClientProperties props,
            String filter,
            String value
    ) {
        return getRecordsWithParams(restClient, props, Map.of(filter, value));
    }

    static <T> List<T> getRecordsWithParams(
            RestClient restClient,
            ClientProperties props,
            Map<String, String> queryParams
    ) {
        return getRecordsWithMultiParams(restClient, props, queryParams.entrySet().stream().
                collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> List.of(entry.getValue())
                )));
    }

    static <T> List<T> getRecordsWithMultiParams(
            RestClient restClient,
            ClientProperties props,
            Map<String, List<String>> queryParams
    ) {
      return restClient.
                get().uri(builder -> builder.
                      path(props.path()).
                      queryParams(CollectionUtils.toMultiValueMap(queryParams)).
                      build()).
                accept(new MediaType(props.media().type(), props.media().subType())).
                header(HEADER_CORRELATION_ID, "correlationId").
                retrieve().body(new ParameterizedTypeReference<>() {});
    }
}
