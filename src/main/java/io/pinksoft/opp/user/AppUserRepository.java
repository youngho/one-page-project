package io.pinksoft.opp.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    Optional<AppUser> findByGoogleId(String googleId);
    boolean existsByUsername(String username);

    @Query("SELECT u.username FROM AppUser u ORDER BY u.username")
    List<String> findAllUsernames();
}
