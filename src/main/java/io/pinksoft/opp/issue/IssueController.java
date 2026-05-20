package io.pinksoft.opp.issue;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/issues")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class IssueController {

    private final IssueService issueService;

    @GetMapping("/{projectId}")
    public ResponseEntity<List<IssueDto>> getIssues(@PathVariable String projectId) {
        return ResponseEntity.ok(issueService.getIssues(projectId));
    }

    @PostMapping("/{projectId}")
    public ResponseEntity<IssueDto> createIssue(
            @PathVariable String projectId,
            @RequestBody IssueDto dto) {
        return ResponseEntity.ok(issueService.createIssue(projectId, dto));
    }

    @PutMapping("/{projectId}/{issueId}")
    public ResponseEntity<?> updateIssue(
            @PathVariable String projectId,
            @PathVariable Long issueId,
            @RequestBody IssueDto dto) {
        try {
            return ResponseEntity.ok(issueService.updateIssue(projectId, issueId, dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{projectId}/{issueId}")
    public ResponseEntity<?> deleteIssue(
            @PathVariable String projectId,
            @PathVariable Long issueId) {
        try {
            issueService.deleteIssue(projectId, issueId);
            return ResponseEntity.ok(Map.of("message", "삭제되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{projectId}/{issueId}/comments")
    public ResponseEntity<List<IssueCommentDto>> getComments(
            @PathVariable String projectId,
            @PathVariable Long issueId) {
        return ResponseEntity.ok(issueService.getComments(issueId));
    }

    @PostMapping("/{projectId}/{issueId}/comments")
    public ResponseEntity<?> addComment(
            @PathVariable String projectId,
            @PathVariable Long issueId,
            @RequestBody IssueCommentDto dto) {
        try {
            return ResponseEntity.ok(issueService.addComment(issueId, dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{projectId}/{issueId}/comments/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable String projectId,
            @PathVariable Long issueId,
            @PathVariable Long commentId) {
        issueService.deleteComment(commentId);
        return ResponseEntity.ok(Map.of("message", "삭제되었습니다."));
    }
}
