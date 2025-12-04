package to.yho.opp.project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import to.yho.opp.project.dto.ProjectDto;
import to.yho.opp.project.entity.Project;
import to.yho.opp.project.repository.ProjectRepository;

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
                .collect(Collectors.toList());

        projectRepository.saveAll(toSave);
    }

    @Transactional
    public ProjectDto saveProject(String username, ProjectDto projectDto) {
        Project project = projectRepository.findByUsernameAndProjectId(username, projectDto.getProjectId())
                .orElseGet(Project::new);

        project.setUsername(username);
        project.setProjectId(projectDto.getProjectId());
        project.setTitle(projectDto.getTitle());
        project.setContent(projectDto.getContent());
//        project.setTags(toJson(projectDto.getTags()));

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
        dto.setContent(project.getContent());
//        dto.setTags(fromJson(project.getTags()));
        return dto;
    }

    private Project toEntity(String username, ProjectDto dto) {
        Project project = new Project();
        project.setUsername(username);
        project.setProjectId(dto.getProjectId());
        project.setTitle(dto.getTitle());
        project.setContent(dto.getContent());
//        project.setTags(toJson(dto.getTags()));
        return project;
    }


}

