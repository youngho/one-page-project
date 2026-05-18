package io.pinksoft.opp.project.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.pinksoft.opp.project.dto.ProjectDto;
import io.pinksoft.opp.project.entity.Project;
import io.pinksoft.opp.project.repository.ProjectRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ObjectMapper objectMapper;

    public List<ProjectDto> getUserProjects(String username) {
        return projectRepository.findByUsername(username)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void saveUserProjects(String username, List<ProjectDto> projectDtos) {
        Map<String, Project> existing = projectRepository.findByUsername(username)
                .stream()
                .collect(Collectors.toMap(Project::getProjectId, p -> p));

        Set<String> incomingIds = projectDtos.stream()
                .map(ProjectDto::getProjectId)
                .collect(Collectors.toSet());

        existing.values().stream()
                .filter(p -> !incomingIds.contains(p.getProjectId()))
                .forEach(projectRepository::delete);

        List<Project> toSave = projectDtos.stream()
                .map(dto -> {
                    Project project = existing.getOrDefault(dto.getProjectId(), new Project());
                    project.setUsername(username);
                    project.setProjectId(dto.getProjectId());
                    project.setTitle(dto.getTitle());
                    project.setContent(dto.getContent());
                    project.setDescription(toJson(dto.getDescription()));
                    project.setUrl(dto.getUrl());
                    return project;
                })
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
        project.setDescription(toJson(projectDto.getDescription()));
        project.setUrl(projectDto.getUrl());

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
        dto.setDescription(fromJson(project.getDescription()));
        dto.setUrl(project.getUrl());
        return dto;
    }

    private String toJson(List<String> list) {
        if (list == null || list.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private List<String> fromJson(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}
