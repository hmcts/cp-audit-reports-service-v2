package uk.gov.hmcts.cp.services;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public abstract class SearchServiceTestBase<SearchService> {

    @Mock
    RestClient restClient;

    @Mock
    RestClient.ResponseSpec responseSpec;

    @Mock
    RestClient.RequestHeadersUriSpec<RestClient.RequestBodySpec> uriSpec;

    UriBuilder uriBuilder = new DefaultUriBuilderFactory().builder();

    String calledUri;

    SearchService underTest;

    abstract SearchService createSearchService();

    @BeforeEach
    void setUp() {

        underTest = createSearchService();

        when(restClient.get()).thenAnswer(i -> uriSpec);

        when(uriSpec.accept(any())).thenAnswer(i -> uriSpec);
        when(uriSpec.header(any(), any())).thenAnswer(i -> uriSpec);
        when(uriSpec.retrieve()).thenReturn(responseSpec);

        when(uriSpec.uri(ArgumentMatchers.<Function<UriBuilder, URI>>any())).thenAnswer(i -> {

            Function<UriBuilder, URI> uriFunction = i.getArgument(0);
            calledUri = uriFunction.apply(uriBuilder).toString();

            return uriSpec;
        });

        calledUri = "";
    }
}
