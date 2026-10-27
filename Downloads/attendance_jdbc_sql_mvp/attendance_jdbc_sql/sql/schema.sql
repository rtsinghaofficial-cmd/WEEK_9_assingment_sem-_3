-- AttendEase database schema (MySQL 8+)
CREATE DATABASE IF NOT EXISTS attendance_db;
USE attendance_db;

CREATE TABLE IF NOT EXISTS students (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(80) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS attendance (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_pk INT NOT NULL,
    attendance_date DATE NOT NULL,
    status ENUM('Present', 'Absent') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_attendance_student
        FOREIGN KEY (student_pk) REFERENCES students(id)
        ON DELETE CASCADE,
    CONSTRAINT uq_student_date UNIQUE (student_pk, attendance_date)
);

-- Optional sample data. Remove these INSERTs if you prefer an empty database.
INSERT IGNORE INTO students (student_id, name)
VALUES ('RA2511047010128', 'RAJ TILAK SINGHA');
