CREATE TABLE photo_evidence (
    id BIGINT NOT NULL AUTO_INCREMENT,
    case_id BIGINT NOT NULL,
    uploaded_by_user_id BIGINT,
    public_upload BOOLEAN NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    size_bytes BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_photo_evidence_case_id (case_id),
    CONSTRAINT fk_photo_evidence_case
        FOREIGN KEY (case_id) REFERENCES inspection_cases (id),
    CONSTRAINT fk_photo_evidence_uploaded_by_user
        FOREIGN KEY (uploaded_by_user_id) REFERENCES users (id)
);
