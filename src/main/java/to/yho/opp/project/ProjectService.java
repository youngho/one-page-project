package to.yho.opp.project;

//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
//    private final ObjectMapper objectMapper;

    public List<ProjectDto> getUserProjects(String username) {
        return projectRepository.findByUsername(username)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void saveUserProjects(String username, List<ProjectDto> projectDtos) {
        List<Project> existing = projectRepository.findByUsername(username);
        projectRepository.deleteAll(existing);

        List<Project> toSave = projectDtos.stream()
                .map(dto -> toEntity(username, dto))
                .toList();

        projectRepository.saveAll(toSave);
    }

    @Transactional
    public ProjectDto saveProject(String username, ProjectDto projectDto) {
        Project project = projectRepository.findByUsernameAndProjectId(username, projectDto.getProjectId())
                .orElseGet(Project::new);

        project.setUsername(username);
        project.setProjectId(projectDto.getProjectId());
        project.setTitle(projectDto.getTitle());
        project.setDescription(projectDto.getDesc());
//        project.setTags(toJson(projectDto.getTags()));
        project.setPositionX(projectDto.getX());
        project.setPositionY(projectDto.getY());

        Project saved = projectRepository.save(project);
        return toDto(saved);
    }

    @Transactional
    public void deleteProject(String username, String projectId) {
        projectRepository.deleteByUsernameAndProjectId(username, projectId);
    }

    private ProjectDto toDto(Project project) {
        ProjectDto dto = new ProjectDto();
        dto.setProjectId(project.getProjectId());
        dto.setTitle(project.getTitle());
        dto.setDesc(project.getDescription());
//        dto.setTags(fromJson(project.getTags()));
        dto.setX(project.getPositionX());
        dto.setY(project.getPositionY());
        return dto;
    }

    private Project toEntity(String username, ProjectDto dto) {
        Project project = new Project();
        project.setUsername(username);
        project.setProjectId(dto.getProjectId());
        project.setTitle(dto.getTitle());
        project.setDescription(dto.getDesc());
//        project.setTags(toJson(dto.getTags()));
        project.setPositionX(dto.getX());
        project.setPositionY(dto.getY());
        return project;
    }

//    private String toJson(List<String> tags) {
//        try {
//            return objectMapper.writeValueAsString(tags);
//        } catch (JsonProcessingException e) {
//            return "[]";
//        }
//    }
//
//    private List<String> fromJson(String json) {
//        try {
//            return objectMapper.readValue(
//                    json,
//                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
//            );
//        } catch (JsonProcessingException e) {
//            return new ArrayList<>();
//        }
//    }
}

