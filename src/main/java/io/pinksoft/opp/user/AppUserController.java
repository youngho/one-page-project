package io.pinksoft.opp.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AppUserController {

    private final AppUserService service;
    private final GoogleTokenVerifier googleTokenVerifier;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        try {
            String username = service.register(body.get("username"), body.get("password"));
            return ResponseEntity.ok(Map.of("username", username));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiMessages.body(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        try {
            String username = service.login(body.get("username"), body.get("password"));
            return ResponseEntity.ok(Map.of("username", username));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiMessages.body(e.getMessage()));
        }
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> body) {
        try {
            String credential = body.get("credential");
            if (credential == null || credential.isBlank()) {
                return ResponseEntity.badRequest().body(ApiMessages.body("Google 인증 정보가 없습니다."));
            }

            GoogleTokenVerifier.GoogleUserInfo googleUser = googleTokenVerifier.verify(credential);
            String username = service.loginOrCreateWithGoogle(
                    googleUser.googleId(), googleUser.email(), googleUser.name());
            return ResponseEntity.ok(Map.of("username", username));
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Google login rejected: {}", e.toString());
            return ResponseEntity.badRequest().body(ApiMessages.body(e.getMessage()));
        } catch (GeneralSecurityException | IOException e) {
            log.warn("Google token verification failed", e);
            return ResponseEntity.badRequest().body(ApiMessages.body("Google 토큰 검증에 실패했습니다."));
        } catch (Exception e) {
            log.error("Google login failed", e);
            return ResponseEntity.badRequest().body(ApiMessages.body("Google 로그인에 실패했습니다."));
        }
    }
}
