CREATE TABLE inspection_case_counters (
    year INT NOT NULL,
    next_value BIGINT NOT NULL,
    PRIMARY KEY (year)
);

CREATE TABLE inspection_cases (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tracking_code VARCHAR(30) NOT NULL,
    applicant_name VARCHAR(150) NOT NULL,
    applicant_phone VARCHAR(40) NOT NULL,
    applicant_email VARCHAR(180),
    address VARCHAR(255) NOT NULL,
    city VARCHAR(120),
    state_region VARCHAR(120),
    description TEXT NOT NULL,
    latitude DECIMAL(10,7),
    longitude DECIMAL(10,7),
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') NOT NULL,
    status ENUM('PENDIENTE', 'ASIGNADO', 'EN_PROCESO', 'INSPECCIONADO', 'CERRADO') NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_inspection_cases_tracking_code (tracking_code)
);
