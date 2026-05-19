package io.pinksoft.opp.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

@Slf4j
@Controller
@RequiredArgsConstructor
public class GoogleAuthCallbackController {

    private final GoogleTokenVerifier googleTokenVerifier;
    private final AppUserService appUserService;

    @PostMapping(value = "/auth/google/callback", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String handleCallback(@RequestParam("credential") String credential) {
        try {
            GoogleTokenVerifier.GoogleUserInfo googleUser = googleTokenVerifier.verify(credential);
            String username = appUserService.loginOrCreateWithGoogle(
                    googleUser.googleId(), googleUser.email(), googleUser.name());
            return "redirect:/?login=" + URLEncoder.encode(username, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Google redirect login failed: {}", e.getMessage());
            return redirectWithError(e.getMessage());
        } catch (GeneralSecurityException | IOException e) {
            log.warn("Google redirect token verification failed", e);
            return redirectWithError("Google 토큰 검증에 실패했습니다.");
        } catch (Exception e) {
            log.error("Google redirect login failed", e);
            return redirectWithError("Google 로그인에 실패했습니다.");
        }
    }

    @GetMapping("/auth/google/callback")
    public String handleGet() {
        return "redirect:/";
    }

    private static String redirectWithError(String message) {
        return "redirect:/?auth_error=" + URLEncoder.encode(message, StandardCharsets.UTF_8);
    }
}
