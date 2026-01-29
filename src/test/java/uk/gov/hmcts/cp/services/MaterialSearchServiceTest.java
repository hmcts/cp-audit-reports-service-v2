package uk.gov.hmcts.cp.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import uk.gov.hmcts.cp.entities.Material;
import uk.gov.hmcts.cp.properties.ClientProperties;
import uk.gov.hmcts.cp.properties.MediaProperties;
import uk.gov.hmcts.cp.properties.ServiceProperties;

import java.net.URI;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MaterialSearchServiceTest {

    @Mock
    RestClient restClient;

    @Mock
    RestClient.ResponseSpec responseSpec;

    @Mock
    RestClient.RequestHeadersUriSpec<RestClient.RequestBodySpec> uriSpec;

    MaterialSearchService underTest;

    UriBuilder uriBuilder = new DefaultUriBuilderFactory().builder();

    String calledUri;

    @BeforeEach
    void setUp() {
        underTest = new MaterialSearchService(restClient, new ServiceProperties(
                "", "", null, null, new ClientProperties("path", new MediaProperties(
                        "application", "json"))));

        when(restClient.get()).thenAnswer(i -> uriSpec);

        when(uriSpec.uri(ArgumentMatchers.<Function<UriBuilder, URI>>any())).thenAnswer(i -> {

            Function<UriBuilder, URI> uriFunction = i.getArgument(0);
            calledUri = uriFunction.apply(uriBuilder).toString();

            return uriSpec;
        });

        when(uriSpec.accept(any())).thenAnswer(i -> uriSpec);
        when(uriSpec.header(any(), any())).thenAnswer(i -> uriSpec);
        when(uriSpec.retrieve()).thenReturn(responseSpec);

        calledUri = "";
    }

    @Test
    void test_getMaterialCases() {

        List<Material> materials = List.of(
                new Material("materialId1", "documentId1", "caseId1", "caseUrn1"),
                new Material("materialId2", "documentId2", "caseId2", "caseUrn2")
        );

        // Given
        when(responseSpec.body(ArgumentMatchers.<ParameterizedTypeReference<List<Material>>>any()))
                .thenReturn(materials);

        // When
        var result = underTest.getMaterialCases("materialId1,materialId2");

        // Then
        assertEquals("path?materialIds=materialId1,materialId2&targetType=CASE_ID", calledUri);
        assertSame(materials, result);
    }
}