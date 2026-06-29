CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(180) NOT NULL,
    phone VARCHAR(40),
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'COORDINATOR', 'ENGINEER') NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email)
);

INSERT INTO users (
    first_name,
    last_name,
    email,
    phone,
    password_hash,
    role,
    status
) VALUES (
    'Admin',
    'Local',
    'admin@puntodeapoyo.local',
    NULL,
    '$2a$10$abcdefghijklmnopqrstuuQkFp1T3u5.wTEPcbZ2NHtBqUx3vDBH2',
    'ADMIN',
    'INACTIVE'
);
