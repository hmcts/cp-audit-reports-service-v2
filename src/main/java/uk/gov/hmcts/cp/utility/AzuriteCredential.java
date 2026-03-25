package uk.gov.hmcts.cp.utility;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import io.jsonwebtoken.Jwts;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class AzuriteCredential implements TokenCredential {

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        return Mono.just(getTokenSync(request));
    }

    @Override
    public AccessToken getTokenSync(TokenRequestContext request) {
        return new AccessToken(jwt(), OffsetDateTime.MAX);
    }

    private static String jwt() {
        return Jwts.builder().
                issuer("https://sts.windows-ppe.net").
                setAudience("https://storage.azure.com").
                issuedAt(Date.from(Instant.now())).
                notBefore(Date.from(Instant.now())).
                expiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS))).
                compact();

    }
}
