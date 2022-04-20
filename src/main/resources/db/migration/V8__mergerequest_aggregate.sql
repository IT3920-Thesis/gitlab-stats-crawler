CREATE TABLE mergerequestaggregate
(
    group_id              VARCHAR(255) NOT NULL,
    project_path          VARCHAR(255) NOT NULL,
    iid                   INT          NOT NULL,

    title                 JSONB        NOT NULL,
    description           JSONB        NOT NULL,

    state                 VARCHAR(255) NOT NULL,
    author                VARCHAR(255) NOT NULL,

    created_at            TIMESTAMP    NOT NULL,
    closed_at             TIMESTAMP    DEFAULT NULL,
    merged_at             TIMESTAMP    DEFAULT NULL,
    updated_at            TIMESTAMP    DEFAULT NULL,

    merged_by             VARCHAR(255) DEFAULT NULL,
    closed_by             VARCHAR(255) DEFAULT NULL,
    assignees             JSONB        DEFAULT NULL,
    reviewers             JSONB        DEFAULT NULL,
    milestones_referenced JSONB        DEFAULT NULL,
    issues_referenced     JSONB        DEFAULT NULL,
    comments              JSONB        DEFAULT NULL,

    PRIMARY KEY (group_id, project_path, iid)
)
