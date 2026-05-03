SET SESSION character_set_client = 'utf8mb4';
SET SESSION character_set_connection = 'utf8mb4';
SET SESSION character_set_results = 'utf8mb4';
SET SESSION collation_connection = 'utf8mb4_unicode_ci';
CREATE TABLE IF NOT EXISTS tasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    completed BOOLEAN DEFAULT FALSE
);
