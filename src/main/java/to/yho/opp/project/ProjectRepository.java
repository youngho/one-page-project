package to.yho.opp.project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByUsername(String username);

    Optional<Project> findByUsernameAndProjectId(String username, String projectId);

    void deleteByUsernameAndProjectId(String username, String projectId);
}

