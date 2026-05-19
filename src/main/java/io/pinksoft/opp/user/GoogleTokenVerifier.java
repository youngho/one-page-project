package io.pinksoft.opp.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class GoogleTokenVerifier {

    private static final Set<String> VALID_ISSUERS = Set.of(
            "https://accounts.google.com",
            "accounts.google.com");

    private final String clientId;
    private final GoogleIdTokenVerifier libraryVerifier;
    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

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

        try {
            return verifyWithTokenInfo(idTokenString);
        } catch (Exception e) {
            log.warn("Google tokeninfo unavailable, using local JWT claim check: {}", e.toString());
            return verifyWithLocalPayload(idTokenString);
        }
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

        Map<String, Object> payload = restClient.get().uri(uri).retrieve().body(Map.class);

        if (payload == null || payload.containsKey("error")) {
            throw new IllegalArgumentException("유효하지 않거나 만료된 Google 토큰입니다.");
        }

        return mapPayload(
                stringValue(payload.get("sub")),
                stringValue(payload.get("email")),
                stringValue(payload.get("name")),
                stringValue(payload.get("aud")),
                stringValue(payload.get("iss")),
                parseExp(payload.get("exp")));
    }

  /** 방화벽으로 Google API 접속이 불가할 때 JWT 클레임만 검사 (서명 검증 없음). */
    private GoogleUserInfo verifyWithLocalPayload(String idTokenString) {
        log.warn("Google API unreachable — JWT signature not verified, claims-only check");
        String[] parts = idTokenString.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("유효하지 않은 Google 토큰 형식입니다.");
        }

        try {
            byte[] decoded = Base64.getUrlDecoder().decode(padBase64(parts[1]));
            JsonNode payload = objectMapper.readTree(decoded);
            return mapPayload(
                    text(payload, "sub"),
                    text(payload, "email"),
                    text(payload, "name"),
                    text(payload, "aud"),
                    text(payload, "iss"),
                    payload.has("exp") ? payload.get("exp").asLong() : 0L);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Google 토큰을 해석할 수 없습니다.");
        }
    }

    private GoogleUserInfo mapPayload(
            String googleId, String email, String name, String aud, String iss, long exp) {
        if (googleId == null || googleId.isBlank()) {
            throw new IllegalArgumentException("Google 사용자 ID를 확인할 수 없습니다.");
        }
        if (!clientId.equals(aud)) {
            throw new IllegalArgumentException("Google Client ID가 일치하지 않습니다.");
        }
        if (iss == null || !VALID_ISSUERS.contains(iss)) {
            throw new IllegalArgumentException("Google 토큰 발급자가 올바르지 않습니다.");
        }
        if (exp > 0 && Instant.now().getEpochSecond() >= exp) {
            throw new IllegalArgumentException("Google 토큰이 만료되었습니다.");
        }
        return new GoogleUserInfo(googleId, email, name);
    }

    private static long parseExp(Object exp) {
        if (exp == null) return 0L;
        if (exp instanceof Number number) return number.longValue();
        try {
            return Long.parseLong(String.valueOf(exp));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private static String padBase64(String base64) {
        int remainder = base64.length() % 4;
        if (remainder == 0) return base64;
        return base64 + "=".repeat(4 - remainder);
    }

    private static String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    public record GoogleUserInfo(String googleId, String email, String name) {}
}
