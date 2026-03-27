package uk.gov.hmcts.cp.properties;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import uk.gov.hmcts.cp.utility.AzuriteCredential;

import java.util.function.Supplier;

public enum CloudType {

    AZURE(() -> new DefaultAzureCredentialBuilder().build()),
    AZURITE(AzuriteCredential::new);

    private final Supplier<TokenCredential> credentialSupplier;

    CloudType(final Supplier<TokenCredential> credentialSupplier) {
        this.credentialSupplier = credentialSupplier;
    }

    public TokenCredential getCredential() {
        return credentialSupplier.get();
    }
}
