CREATE TABLE commitaggregate
(
    group_id                 VARCHAR(255) NOT NULL,
    project_path             VARCHAR(255) NOT NULL,
    commit_sha               VARCHAR(255) NOT NULL,
    author_email             VARCHAR(255) NOT NULL,
    commit_time              TIMESTAMP    NOT NULL,

    size                     VARCHAR(255) NOT NULL,
    title                    JSONB        NOT NULL,
    message                  JSONB                 DEFAULT NULL,

    files_changed            INT          NOT NULL DEFAULT 0,
    test_classification      VARCHAR(255) NOT NULL,
    gitlab_issues_referenced JSONB        NOT NULL,
    is_merge_commit          boolean      NOT NULL DEFAULT false,

    PRIMARY KEY (group_id, project_path, commit_sha, author_email)
);
