package uk.gov.hmcts.cp.config;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.data.tables.TableClient;
import com.azure.data.tables.TableServiceClient;
import com.azure.data.tables.TableServiceClientBuilder;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.BlobServiceVersion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.cp.properties.AzureProperties;
import uk.gov.hmcts.cp.properties.CloudType;
import uk.gov.hmcts.cp.properties.FabricProperties;
import uk.gov.hmcts.cp.properties.ServiceProperties;
import uk.gov.hmcts.cp.properties.TokenType;

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
    public TokenType tokenType(final AzureProperties settings) {
        return settings.tokenType();
    }

    @Bean
    public CloudType credentialType(final AzureProperties settings) {
        return settings.cloudType();
    }

    @Bean
    public TokenCredential azureCredential(final CloudType cloudType) {
        return cloudType.getCredential();
    }

    @Bean
    public Function<TokenRequestContext, AccessToken> azureAccess(
            final TokenType tokenType,
            final TokenCredential azureCredential
    ) {
        return tokenType.getFunction(azureCredential);
    }

    @Bean
    public BlobServiceClient blobServiceClient(
            final AzureProperties settings,
            final TokenCredential azureCredential
    ) {
        return new BlobServiceClientBuilder().
                serviceVersion(BlobServiceVersion.V2025_07_05).
                endpoint(settings.blobEndpoint()).
                credential(azureCredential).
                buildClient();
    }

    @Bean
    public TableServiceClient tableServiceClient(
            final AzureProperties settings,
            final TokenCredential azureCredential
    ) {
        return new TableServiceClientBuilder().
                endpoint(settings.tableEndpoint()).
                credential(azureCredential).
                buildClient();
    }

    @Bean
    public TableClient reportRequests(
            final AzureProperties settings,
            final TableServiceClient tableServiceClient
    ) {
        return tableServiceClient.getTableClient(settings.tableName());
    }

    @Bean
    public TokenRequestContext tokenRequest(final AzureProperties settings) {
        return new TokenRequestContext().setScopes(settings.scopes());
    }

    @Bean
    public AzureProperties azureProperties(final ServiceProperties settings) {
        return settings.azure();
    }

    @Bean
    public FabricProperties fabricProperties(final AzureProperties settings) {
        return settings.fabric();
    }
}
