CREATE TABLE technical_observations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    case_id BIGINT NOT NULL,
    created_by_user_id BIGINT NOT NULL,
    observations TEXT NOT NULL,
    recommendations TEXT NOT NULL,
    structural_risk ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_technical_observations_case_id (case_id),
    CONSTRAINT fk_technical_observations_case
        FOREIGN KEY (case_id) REFERENCES inspection_cases (id),
    CONSTRAINT fk_technical_observations_created_by_user
        FOREIGN KEY (created_by_user_id) REFERENCES users (id)
);

CREATE TABLE technical_observation_photos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    technical_observation_id BIGINT NOT NULL,
    photo_evidence_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_technical_observation_photos_photo (photo_evidence_id),
    KEY idx_technical_observation_photos_observation_id (technical_observation_id),
    CONSTRAINT fk_technical_observation_photos_observation
        FOREIGN KEY (technical_observation_id) REFERENCES technical_observations (id),
    CONSTRAINT fk_technical_observation_photos_photo
        FOREIGN KEY (photo_evidence_id) REFERENCES photo_evidence (id)
);
