CREATE TABLE case_assignments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    case_id BIGINT NOT NULL,
    engineer_id BIGINT NOT NULL,
    assigned_by BIGINT NOT NULL,
    assigned_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_case_assignments_case_engineer (case_id, engineer_id),
    KEY idx_case_assignments_case_id (case_id),
    KEY idx_case_assignments_engineer_id (engineer_id),
    CONSTRAINT fk_case_assignments_case
        FOREIGN KEY (case_id) REFERENCES inspection_cases (id),
    CONSTRAINT fk_case_assignments_engineer
        FOREIGN KEY (engineer_id) REFERENCES users (id),
    CONSTRAINT fk_case_assignments_assigned_by
        FOREIGN KEY (assigned_by) REFERENCES users (id)
);
