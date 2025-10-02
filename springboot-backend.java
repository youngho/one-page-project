// 1. Entity Class - Project.java
package com.portfolio.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "projects")
@Data
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String projectId; // 프론트엔드의 'p1', 'p2' 등
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(columnDefinition = "TEXT")
    private String tags; // JSON 형식으로 저장
    
    @Column(nullable = false)
    private Double positionX;
    
    @Column(nullable = false)
    private Double positionY;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

// 2. Repository Interface - ProjectRepository.java
package com.portfolio.repository;

import com.portfolio.entity.Project;
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

// 3. DTO Classes - ProjectDTO.java
package com.portfolio.dto;

import lombok.Data;
import java.util.List;

@Data
public class ProjectDTO {
    private String projectId;
    private String title;
    private String desc;
    private List<String> tags;
    private Double x;
    private Double y;
}

@Data
class ProjectsRequest {
    private String username;
    private List<ProjectDTO> projects;
}

@Data
class ProjectsResponse {
    private String username;
    private List<ProjectDTO> projects;
}

// 4. Service Class - ProjectService.java
package com.portfolio.service;

import com.portfolio.dto.ProjectDTO;
import com.portfolio.entity.Project;
import com.portfolio.repository.ProjectRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // 사용자의 모든 프로젝트 조회
    public List<ProjectDTO> getUserProjects(String username) {
        List<Project> projects = projectRepository.findByUsername(username);
        return projects.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    // 프로젝트 저장 (일괄)
    @Transactional
    public void saveUserProjects(String username, List<ProjectDTO> projectDTOs) {
        // 기존 프로젝트 삭제
        List<Project> existingProjects = projectRepository.findByUsername(username);
        projectRepository.deleteAll(existingProjects);
        
        // 새 프로젝트 저장
        List<Project> projects = projectDTOs.stream()
                .map(dto -> convertToEntity(username, dto))
                .collect(Collectors.toList());
        
        projectRepository.saveAll(projects);
    }
    
    // 단일 프로젝트 저장/업데이트
    @Transactional
    public ProjectDTO saveProject(String username, ProjectDTO projectDTO) {
        Project project = projectRepository
                .findByUsernameAndProjectId(username, projectDTO.getProjectId())
                .orElse(new Project());
        
        project.setUsername(username);
        project.setProjectId(projectDTO.getProjectId());
        project.setTitle(projectDTO.getTitle());
        project.setDescription(projectDTO.getDesc());
        project.setTags(convertTagsToJson(projectDTO.getTags()));
        project.setPositionX(projectDTO.getX());
        project.setPositionY(projectDTO.getY());
        
        Project saved = projectRepository.save(project);
        return convertToDTO(saved);
    }
    
    // 프로젝트 삭제
    @Transactional
    public void deleteProject(String username, String projectId) {
        projectRepository.deleteByUsernameAndProjectId(username, projectId);
    }
    
    // Entity -> DTO 변환
    private ProjectDTO convertToDTO(Project project) {
        ProjectDTO dto = new ProjectDTO();
        dto.setProjectId(project.getProjectId());
        dto.setTitle(project.getTitle());
        dto.setDesc(project.getDescription());
        dto.setTags(convertJsonToTags(project.getTags()));
        dto.setX(project.getPositionX());
        dto.setY(project.getPositionY());
        return dto;
    }
    
    // DTO -> Entity 변환
    private Project convertToEntity(String username, ProjectDTO dto) {
        Project project = new Project();
        project.setUsername(username);
        project.setProjectId(dto.getProjectId());
        project.setTitle(dto.getTitle());
        project.setDescription(dto.getDesc());
        project.setTags(convertTagsToJson(dto.getTags()));
        project.setPositionX(dto.getX());
        project.setPositionY(dto.getY());
        return project;
    }
    
    // Tags를 JSON 문자열로 변환
    private String convertTagsToJson(List<String> tags) {
        try {
            return objectMapper.writeValueAsString(tags);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }
    
    // JSON 문자열을 Tags 리스트로 변환
    private List<String> convertJsonToTags(String json) {
        try {
            return objectMapper.readValue(json, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }
}

// 5. Controller Class - ProjectController.java
package com.portfolio.controller;

import com.portfolio.dto.ProjectDTO;
import com.portfolio.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "*") // CORS 허용
public class ProjectController {
    
    @Autowired
    private ProjectService projectService;
    
    // 사용자의 모든 프로젝트 조회
    @GetMapping("/{username}")
    public ResponseEntity<Map<String, Object>> getUserProjects(@PathVariable String username) {
        List<ProjectDTO> projects = projectService.getUserProjects(username);
        
        Map<String, Object> response = new HashMap<>();
        response.put("username", username);
        
        // List를 Map으로 변환 (프론트엔드 형식에 맞춤)
        Map<String, ProjectDTO> projectsMap = new HashMap<>();
        for (ProjectDTO project : projects) {
            projectsMap.put(project.getProjectId(), project);
        }
        response.put("projects", projectsMap);
        
        return ResponseEntity.ok(response);
    }
    
    // 사용자의 모든 프로젝트 저장
    @PostMapping("/{username}")
    public ResponseEntity<Map<String, String>> saveUserProjects(
            @PathVariable String username,
            @RequestBody Map<String, ProjectDTO> projects) {
        
        List<ProjectDTO> projectList = new ArrayList<>(projects.values());
        projectService.saveUserProjects(username, projectList);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "프로젝트가 저장되었습니다.");
        response.put("username", username);
        
        return ResponseEntity.ok(response);
    }
    
    // 단일 프로젝트 저장/업데이트
    @PutMapping("/{username}/{projectId}")
    public ResponseEntity<ProjectDTO> saveProject(
            @PathVariable String username,
            @PathVariable String projectId,
            @RequestBody ProjectDTO projectDTO) {
        
        projectDTO.setProjectId(projectId);
        ProjectDTO saved = projectService.saveProject(username, projectDTO);
        
        return ResponseEntity.ok(saved);
    }
    
    // 프로젝트 삭제
    @DeleteMapping("/{username}/{projectId}")
    public ResponseEntity<Map<String, String>> deleteProject(
            @PathVariable String username,
            @PathVariable String projectId) {
        
        projectService.deleteProject(username, projectId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "프로젝트가 삭제되었습니다.");
        
        return ResponseEntity.ok(response);
    }
}

// 6. Application Configuration - application.yml
/*
spring:
  datasource:
    url: jdbc:mariadb://localhost:3306/portfolio_db
    username: root
    password: your_password
    driver-class-name: org.mariadb.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MariaDBDialect
        format_sql: true
    
server:
  port: 8080

logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
*/

// 7. Main Application Class - PortfolioApplication.java
package com.portfolio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PortfolioApplication {
    public static void main(String[] args) {
        SpringApplication.run(PortfolioApplication.class, args);
    }
}