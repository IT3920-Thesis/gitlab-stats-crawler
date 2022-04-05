CREATE TABLE projectmilestoneaggregate
(
    group_id      VARCHAR(255) NOT NULL,
    project_path  VARCHAR(255) NOT NULL,
    milestone_iid INT          NOT NULL,

    title         JSONB        NOT NULL,
    description   JSONB        NOT NULL,

    created_at    TIMESTAMP    NOT NULL,
    start_date    TIMESTAMP DEFAULT NULL,
    due_date      TIMESTAMP DEFAULT NULL,
    closed_at     TIMESTAMP DEFAULT NULL,
    expired       BOOLEAN   NOT NULL DEFAULT FALSE,

    PRIMARY KEY (group_id, project_path, milestone_iid)
)
