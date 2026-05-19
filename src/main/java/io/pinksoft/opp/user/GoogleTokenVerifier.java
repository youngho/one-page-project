package io.pinksoft.opp.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@Component
public class GoogleTokenVerifier {

    private final RestClient restClient = RestClient.create();
    private final String clientId;

    public GoogleTokenVerifier(@Value("${google.client-id:}") String clientId) {
        this.clientId = clientId;
    }

    public GoogleUserInfo verify(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new IllegalArgumentException("Google 인증 정보가 없습니다.");
        }
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalStateException("Google Client ID가 서버에 설정되지 않았습니다.");
        }

        URI uri = UriComponentsBuilder
                .fromUriString("https://oauth2.googleapis.com/tokeninfo")
                .queryParam("id_token", idToken)
                .build()
                .toUri();

        @SuppressWarnings("unchecked")
        Map<String, Object> payload = restClient.get()
                .uri(uri)
                .retrieve()
                .body(Map.class);

        if (payload == null || payload.containsKey("error")) {
            throw new IllegalArgumentException("유효하지 않은 Google 토큰입니다.");
        }

        String aud = stringValue(payload.get("aud"));
        if (!clientId.equals(aud)) {
            throw new IllegalArgumentException("Google Client ID가 일치하지 않습니다.");
        }

        String googleId = stringValue(payload.get("sub"));
        if (googleId == null || googleId.isBlank()) {
            throw new IllegalArgumentException("Google 사용자 ID를 확인할 수 없습니다.");
        }

        return new GoogleUserInfo(
                googleId,
                stringValue(payload.get("email")),
                stringValue(payload.get("name"))
        );
    }

    private static String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    public record GoogleUserInfo(String googleId, String email, String name) {}
}
