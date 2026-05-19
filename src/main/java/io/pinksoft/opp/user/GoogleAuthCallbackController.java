package io.pinksoft.opp.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class GoogleAuthCallbackController {

    private final GoogleAuthRedirectHandler redirectHandler;

    @PostMapping(value = "/auth/google/callback", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String handleCallback(@RequestParam("credential") String credential) {
        return "redirect:" + redirectHandler.redirectUrlAfterLogin(credential);
    }

    @GetMapping("/auth/google/callback")
    public String handleGet() {
        return "redirect:/";
    }
}
