package uk.gov.hmcts.cp.config;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.cp.properties.AzureProperties;
import uk.gov.hmcts.cp.properties.FabricProperties;
import uk.gov.hmcts.cp.properties.ServiceProperties;

import java.util.function.Function;

@Configuration
public class ClientConfig {

    private static final String HEADER_USER = "CJSCPPUID";

    @Bean
    public RestClient restClient(final ServiceProperties settings) {
        return RestClient.builder().
                baseUrl(settings.baseUrl()).
                defaultHeader(HEADER_USER, settings.cjsCpUid()).
                build();
    }

    @Bean
    public Function<TokenRequestContext, AccessToken> azureAccess(
            final AzureProperties settings,
            final DefaultAzureCredential azureCredential
    ) {
        return settings.tokenType().getFunction(azureCredential);
    }

    @Bean
    public TokenRequestContext tokenRequest(final AzureProperties settings) {
        return new TokenRequestContext().setScopes(settings.scopes());
    }

    @Bean
    public AzureProperties audienceProperties(final ServiceProperties settings) {
        return settings.azure();
    }

    @Bean
    public FabricProperties fabricProperties(final AzureProperties settings) {
        return settings.fabric();
    }

    @Bean
    public DefaultAzureCredential azureClient(final DefaultAzureCredentialBuilder builder) {
        return builder.build();
    }

    @Bean
    public DefaultAzureCredentialBuilder azureClientBuilder() {
        return new DefaultAzureCredentialBuilder();
    }
}
