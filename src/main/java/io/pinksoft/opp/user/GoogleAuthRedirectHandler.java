package io.pinksoft.opp.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleAuthRedirectHandler {

    private final GoogleTokenVerifier googleTokenVerifier;
    private final AppUserService appUserService;

    public String redirectUrlAfterLogin(String credential) {
        try {
            GoogleTokenVerifier.GoogleUserInfo googleUser = googleTokenVerifier.verify(credential);
            String username = appUserService.loginOrCreateWithGoogle(
                    googleUser.googleId(), googleUser.email(), googleUser.name());
            return "/?login=" + URLEncoder.encode(username, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Google redirect login failed: {}", e.getMessage());
            return errorUrl(e.getMessage());
        } catch (GeneralSecurityException | IOException e) {
            log.warn("Google redirect token verification failed", e);
            return errorUrl("Google 토큰 검증에 실패했습니다.");
        } catch (Exception e) {
            log.error("Google redirect login failed", e);
            return errorUrl("Google 로그인에 실패했습니다.");
        }
    }

    private static String errorUrl(String message) {
        return "/?auth_error=" + URLEncoder.encode(message, StandardCharsets.UTF_8);
    }
}
