package io.pinksoft.opp.user;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Map;

@Slf4j
@Component
public class GoogleTokenVerifier {

    private final String clientId;
    private final GoogleIdTokenVerifier libraryVerifier;
    private final RestClient restClient;

    public GoogleTokenVerifier(@Value("${google.client-id:}") String clientId) {
        this.clientId = clientId == null ? "" : clientId.trim();
        if (this.clientId.isEmpty()) {
            throw new IllegalStateException("google.client-id가 설정되지 않았습니다.");
        }
        this.libraryVerifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(this.clientId))
                .build();
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(10_000);
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    public GoogleUserInfo verify(String idTokenString) throws IOException, GeneralSecurityException {
        if (idTokenString == null || idTokenString.isBlank()) {
            throw new IllegalArgumentException("Google 인증 정보가 없습니다.");
        }

        try {
            GoogleUserInfo fromLibrary = verifyWithLibrary(idTokenString);
            if (fromLibrary != null) {
                return fromLibrary;
            }
        } catch (IOException | GeneralSecurityException e) {
            log.warn("Google library token verify failed, trying tokeninfo: {}", e.toString());
        }

        return verifyWithTokenInfo(idTokenString);
    }

    private GoogleUserInfo verifyWithLibrary(String idTokenString) throws GeneralSecurityException, IOException {
        GoogleIdToken idToken = libraryVerifier.verify(idTokenString);
        if (idToken == null) {
            return null;
        }
        GoogleIdToken.Payload payload = idToken.getPayload();
        return new GoogleUserInfo(
                payload.getSubject(),
                payload.getEmail(),
                stringValue(payload.get("name")));
    }

    @SuppressWarnings("unchecked")
    private GoogleUserInfo verifyWithTokenInfo(String idTokenString) {
        URI uri = UriComponentsBuilder
                .fromUriString("https://oauth2.googleapis.com/tokeninfo")
                .queryParam("id_token", idTokenString)
                .build()
                .toUri();

        Map<String, Object> payload;
        try {
            payload = restClient.get().uri(uri).retrieve().body(Map.class);
        } catch (Exception e) {
            log.error("Google tokeninfo request failed", e);
            throw new IllegalArgumentException("Google 서버에 연결할 수 없습니다. 방화벽에서 oauth2.googleapis.com 허용이 필요합니다.");
        }

        if (payload == null || payload.containsKey("error")) {
            throw new IllegalArgumentException("유효하지 않거나 만료된 Google 토큰입니다.");
        }

        if (!clientId.equals(stringValue(payload.get("aud")))) {
            throw new IllegalArgumentException("Google Client ID가 일치하지 않습니다.");
        }

        String googleId = stringValue(payload.get("sub"));
        if (googleId == null || googleId.isBlank()) {
            throw new IllegalArgumentException("Google 사용자 ID를 확인할 수 없습니다.");
        }

        return new GoogleUserInfo(
                googleId,
                stringValue(payload.get("email")),
                stringValue(payload.get("name")));
    }

    private static String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    public record GoogleUserInfo(String googleId, String email, String name) {}
}
