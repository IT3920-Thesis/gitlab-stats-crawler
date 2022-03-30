CREATE TABLE issueaggregate
(
    group_id     VARCHAR(255) NOT NULL,
    project_path VARCHAR(255) NOT NULL,
    issue_iid    INT          NOT NULL,
    title        JSONB        NOT NULL,
    description  JSONB        NOT NULL,

    created_at   TIMESTAMP    NOT NULL,
    author       VARCHAR(255) NOT NULL,
    closed_at    TIMESTAMP    DEFAULT NULL,
    closed_by    VARCHAR(255) DEFAULT NULL,
    state        VARCHAR(255) NOT NULL,
    labels       JSONB        NOT NULL,
    assignees    JSONB        NOT NULL,
    notes        JSONB        NOT NULL,

    PRIMARY KEY (group_id, project_path, issue_iid)
);
