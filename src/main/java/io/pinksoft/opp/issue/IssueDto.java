package io.pinksoft.opp.issue;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IssueDto {
    private Long id;
    private String projectId;
    private String createdBy;
    private String title;
    private String description;
    private String status;
    private String priority;
    private String assignee;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
