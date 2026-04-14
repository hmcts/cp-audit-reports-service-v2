package uk.gov.hmcts.cp.properties;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;

import java.time.OffsetDateTime;
import java.util.function.Function;

public enum TokenType {

    AZURE(azureCredential -> azureCredential::getTokenSync),
    TEST(_ -> _ -> getTestToken());

    private final Function<TokenCredential, Function<TokenRequestContext, AccessToken>> function;

    private static final AccessToken TEST_TOKEN = new AccessToken("TEST", OffsetDateTime.now().plusHours(1));

    TokenType(final Function<TokenCredential, Function<TokenRequestContext, AccessToken>> function) {
        this.function = function;
    }

    public Function<TokenRequestContext, AccessToken> getFunction(final TokenCredential azureCredential) {
        return function.apply(azureCredential);
    }

    private static AccessToken getTestToken() {
        return TEST_TOKEN;
    }
}
