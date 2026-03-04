package uk.gov.hmcts.cp.utility;

import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.cp.properties.ClientProperties;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface RecordUtils {

    String HEADER_CORRELATION_ID = "CPCLIENTCORRELATIONID";

    static <T> T getRecords(
            final RestClient restClient,
            final ClientProperties props,
            final String filter,
            final String value,
            final Class<T> bodyType
    ) {
        return getRecordsWithParams(restClient, props, Map.of(filter, value), bodyType);
    }

    static <T> T getRecordsWithParams(
            final RestClient restClient,
            final ClientProperties props,
            final Map<String, String> queryParams,
            final Class<T> bodyType
    ) {
        return getRecordsWithMultiParams(restClient, props, queryParams.entrySet().stream().
                collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> List.of(entry.getValue())
                )), bodyType);
    }

    static <T> T getRecordsWithMultiParams(
            final RestClient restClient,
            final ClientProperties props,
            final Map<String, List<String>> queryParams,
            final Class<T> bodyType
    ) {
        return restClient.
                get().uri(builder -> builder.
                        path(props.path()).
                        queryParams(CollectionUtils.toMultiValueMap(queryParams)).
                        build()
                ).
                accept(new MediaType(props.media().type(), props.media().subType())).
                header(HEADER_CORRELATION_ID, "correlationId").
                retrieve().body(bodyType);
    }
}
