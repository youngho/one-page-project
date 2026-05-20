package io.pinksoft.opp.issue;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IssueService {

    private final IssueRepository issueRepository;
    private final IssueCommentRepository commentRepository;

    public List<IssueDto> getIssues(String projectId) {
        return issueRepository.findByProjectIdOrderByCreatedAtDesc(projectId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public IssueDto createIssue(String projectId, IssueDto dto) {
        Issue issue = new Issue();
        issue.setProjectId(projectId);
        issue.setCreatedBy(dto.getCreatedBy());
        issue.setTitle(dto.getTitle());
        issue.setDescription(dto.getDescription());
        issue.setStatus(dto.getStatus() != null ? dto.getStatus() : "OPEN");
        issue.setPriority(dto.getPriority() != null ? dto.getPriority() : "NORMAL");
        issue.setAssignee(dto.getAssignee());
        return toDto(issueRepository.save(issue));
    }

    @Transactional
    public IssueDto updateIssue(String projectId, Long issueId, IssueDto dto) {
        Issue issue = issueRepository.findById(issueId)
                .filter(i -> i.getProjectId().equals(projectId))
                .orElseThrow(() -> new IllegalArgumentException("이슈를 찾을 수 없습니다."));
        issue.setTitle(dto.getTitle());
        issue.setDescription(dto.getDescription());
        issue.setStatus(dto.getStatus());
        issue.setPriority(dto.getPriority());
        issue.setAssignee(dto.getAssignee());
        return toDto(issueRepository.save(issue));
    }

    @Transactional
    public void deleteIssue(String projectId, Long issueId) {
        Issue issue = issueRepository.findById(issueId)
                .filter(i -> i.getProjectId().equals(projectId))
                .orElseThrow(() -> new IllegalArgumentException("이슈를 찾을 수 없습니다."));
        commentRepository.deleteAll(commentRepository.findByIssueIdOrderByCreatedAtAsc(issueId));
        issueRepository.delete(issue);
    }

    public List<IssueCommentDto> getComments(Long issueId) {
        return commentRepository.findByIssueIdOrderByCreatedAtAsc(issueId)
                .stream().map(this::toCommentDto).collect(Collectors.toList());
    }

    @Transactional
    public IssueCommentDto addComment(Long issueId, IssueCommentDto dto) {
        if (!issueRepository.existsById(issueId)) {
            throw new IllegalArgumentException("이슈를 찾을 수 없습니다.");
        }
        IssueComment comment = new IssueComment();
        comment.setIssueId(issueId);
        comment.setUsername(dto.getUsername());
        comment.setContent(dto.getContent());
        return toCommentDto(commentRepository.save(comment));
    }

    @Transactional
    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    private IssueDto toDto(Issue issue) {
        IssueDto dto = new IssueDto();
        dto.setId(issue.getId());
        dto.setProjectId(issue.getProjectId());
        dto.setCreatedBy(issue.getCreatedBy());
        dto.setTitle(issue.getTitle());
        dto.setDescription(issue.getDescription());
        dto.setStatus(issue.getStatus());
        dto.setPriority(issue.getPriority());
        dto.setAssignee(issue.getAssignee());
        dto.setCreatedAt(issue.getCreatedAt());
        dto.setUpdatedAt(issue.getUpdatedAt());
        return dto;
    }

    private IssueCommentDto toCommentDto(IssueComment comment) {
        IssueCommentDto dto = new IssueCommentDto();
        dto.setId(comment.getId());
        dto.setIssueId(comment.getIssueId());
        dto.setUsername(comment.getUsername());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        return dto;
    }
}
