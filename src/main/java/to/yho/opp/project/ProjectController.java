package to.yho.opp.project;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping("/{username}")
    public ResponseEntity<Map<String, Object>> getUserProjects(@PathVariable String username) {
        List<ProjectDto> projects = projectService.getUserProjects(username);

        Map<String, ProjectDto> projectMap = new HashMap<>();
        for (ProjectDto project : projects) {
            projectMap.put(project.getProjectId(), project);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("username", username);
        response.put("projects", projectMap);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{username}")
    public ResponseEntity<Map<String, String>> saveUserProjects(
            @PathVariable String username,
            @RequestBody Map<String, ProjectDto> projects
    ) {
        projectService.saveUserProjects(username, new ArrayList<>(projects.values()));

        Map<String, String> response = new HashMap<>();
        response.put("message", "프로젝트가 저장되었습니다.");
        response.put("username", username);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{username}/{projectId}")
    public ResponseEntity<ProjectDto> saveProject(
            @PathVariable String username,
            @PathVariable String projectId,
            @RequestBody ProjectDto projectDto
    ) {
        projectDto.setProjectId(projectId);
        ProjectDto saved = projectService.saveProject(username, projectDto);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{username}/{projectId}")
    public ResponseEntity<Map<String, String>> deleteProject(
            @PathVariable String username,
            @PathVariable String projectId
    ) {
        projectService.deleteProject(username, projectId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "프로젝트가 삭제되었습니다.");
        response.put("username", username);

        return ResponseEntity.ok(response);
    }
}

