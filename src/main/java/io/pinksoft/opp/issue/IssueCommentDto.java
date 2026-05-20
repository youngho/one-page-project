package io.pinksoft.opp.issue;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IssueCommentDto {
    private Long id;
    private Long issueId;
    private String username;
    private String content;
    private LocalDateTime createdAt;
}
