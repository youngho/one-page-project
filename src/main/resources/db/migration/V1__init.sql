CREATE TABLE IF NOT EXISTS projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    project_id VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    desc TEXT,
    position_x DOUBLE NOT NULL,
    position_y DOUBLE NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_project_id (project_id),
    UNIQUE KEY uk_username_projectid (username, project_id)
) ENGINE=InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

INSERT INTO projects (username, project_id, title, content, desc, position_x, position_y)
SELECT 'testuser', 'p1', '🎨 첫 프로젝트', '여기에 프로젝트를 추가해보세요!', '["시작하기"]', 400, 300
WHERE NOT EXISTS (
    SELECT 1 FROM projects WHERE username = 'testuser' AND project_id = 'p1'
);

INSERT INTO projects (username, project_id, title, content, desc, position_x, position_y)
SELECT 'testuser', 'p2', '🚀 두 번째 프로젝트', '멋진 프로젝트입니다', '["React", "Spring Boot"]', 600, 400
WHERE NOT EXISTS (
    SELECT 1 FROM projects WHERE username = 'testuser' AND project_id = 'p2'
);
