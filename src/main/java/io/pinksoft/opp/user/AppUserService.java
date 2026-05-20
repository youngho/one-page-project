package io.pinksoft.opp.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppUserService {

    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public String register(String username, String password) {
        if (username == null || username.isBlank()) throw new IllegalArgumentException("사용자명을 입력하세요.");
        if (password == null || password.isBlank()) throw new IllegalArgumentException("비밀번호를 입력하세요.");
        if (repository.existsByUsername(username)) throw new IllegalArgumentException("이미 사용 중인 사용자명입니다.");

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        repository.save(user);
        return username;
    }

    public String login(String username, String password) {
        AppUser user = repository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (user.getPassword() == null) throw new IllegalArgumentException("Google 계정으로 로그인하세요.");
        if (!passwordEncoder.matches(password, user.getPassword())) throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        return username;
    }

    @Transactional
    public String loginOrCreateWithGoogle(String googleId, String email, String name) {
        return repository.findByGoogleId(googleId)
                .map(AppUser::getUsername)
                .orElseGet(() -> {
                    String base = deriveUsernameBase(googleId, email);
                    String username = base;
                    int i = 1;
                    while (repository.existsByUsername(username)) username = base + i++;

                    AppUser user = new AppUser();
                    user.setUsername(username);
                    user.setGoogleId(googleId);
                    user.setEmail(email);
                    repository.save(user);
                    return username;
                });
    }

    public List<String> listUsernames() {
        return repository.findAllUsernames();
    }

    private static String deriveUsernameBase(String googleId, String email) {
        if (email != null && email.contains("@")) {
            String local = email.substring(0, email.indexOf('@'));
            String sanitized = local.replaceAll("[^a-zA-Z0-9_]", "_");
            if (!sanitized.isBlank()) return sanitized;
        }
        String suffix = googleId.length() > 8 ? googleId.substring(0, 8) : googleId;
        return "user_" + suffix.replaceAll("[^a-zA-Z0-9_]", "_");
    }
}
