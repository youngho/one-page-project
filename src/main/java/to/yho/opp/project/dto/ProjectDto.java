package to.yho.opp.project.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProjectDto {
    private String projectId;
    private String title;
    private String desc;
    private List<String> tags;
}

