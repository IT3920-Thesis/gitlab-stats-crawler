-- Contains extracted and classified contributions for a changed file, done in a specific commit, by a certain author, ..in a specific repository
CREATE TABLE changecontribution(
    group_id VARCHAR(255) NOT NULL,
    repository_id VARCHAR(255) NOT NULL,
    author_email VARCHAR(255) NOT NULL,
    commit_sha VARCHAR(255) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    type VARCHAR(50),
    timestamp TIMESTAMP NOT NULL,
    lines_added BIGINT NOT NULL DEFAULT 0,
    lines_removed BIGINT NOT NULL DEFAULT 0,
    previous_file_path VARCHAR(255) DEFAULT NULL,

    PRIMARY KEY (group_id, repository_id, author_email, commit_sha, file_path)
);
