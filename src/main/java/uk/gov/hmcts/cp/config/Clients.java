package uk.gov.hmcts.cp.config;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.data.tables.TableClient;
import com.azure.data.tables.TableServiceClient;
import com.azure.data.tables.TableServiceClientBuilder;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.cp.properties.FabricProperties;
import uk.gov.hmcts.cp.properties.ServiceProperties;
import uk.gov.hmcts.cp.properties.TableProperties;

import java.util.function.Function;

@Configuration
public class Clients {

    private static final String HEADER_USER = "CJSCPPUID";

    @Bean
    public RestClient restClient(final ServiceProperties settings) {
        return RestClient.builder().
                baseUrl(settings.baseUrl()).
                defaultHeader(HEADER_USER, settings.cjsCpUid()).
                build();
    }

    @Bean
    public TableServiceClient tableServiceClient(
            final TableProperties settings,
            final AzureNamedKeyCredential credential
    ) {
        return new TableServiceClientBuilder().
                endpoint(settings.endpoint()).
//              sasToken(settings.sasToken()).
                credential(credential).
                buildClient();
    }

    @Bean
    @Qualifier("reportrequests")
    public TableClient reportRequests(final TableServiceClient tableServiceClient) {
        return tableServiceClient.getTableClient("reportrequests");
    }

    @Bean
    public Function<TokenRequestContext, AccessToken> azureAccess(
            final ServiceProperties settings,
            final DefaultAzureCredential azureCredential
    ) {
        return settings.tokenType().getFunction(azureCredential);
    }

    @Bean
    public TokenRequestContext tokenRequest(final ServiceProperties settings) {
        return new TokenRequestContext().setScopes(settings.scopes());
    }

    @Bean
    public FabricProperties fabricProperties(final ServiceProperties settings) {
        return settings.fabric();
    }

    @Bean
    public TableProperties tableProperties(final ServiceProperties settings) {
        return settings.table();
    }

    @Bean
    public DefaultAzureCredential azureClient(final DefaultAzureCredentialBuilder builder) {
        return builder.build();
    }

    @Bean
    public DefaultAzureCredentialBuilder azureClientBuilder() {
        return new DefaultAzureCredentialBuilder();
    }

    @Bean
    public AzureNamedKeyCredential azureCredential(final TableProperties settings) {
        return new AzureNamedKeyCredential(settings.azureName(), settings.azureKey());
    }

}
