package io.pinksoft.opp.user;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Component
public class GoogleTokenVerifier {

    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifier(@Value("${google.client-id:}") String clientId) {
        String audience = clientId == null ? "" : clientId.trim();
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(audience))
                .build();
    }

    public GoogleUserInfo verify(String idTokenString) throws GeneralSecurityException, IOException {
        if (idTokenString == null || idTokenString.isBlank()) {
            throw new IllegalArgumentException("Google 인증 정보가 없습니다.");
        }

        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken == null) {
            throw new IllegalArgumentException("유효하지 않거나 만료된 Google 토큰입니다.");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        return new GoogleUserInfo(
                payload.getSubject(),
                payload.getEmail(),
                (String) payload.get("name"));
    }

    public record GoogleUserInfo(String googleId, String email, String name) {}
}
