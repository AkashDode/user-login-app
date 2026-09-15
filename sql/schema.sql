-- ---------------------------------------------------------------------------
-- User Login Application — database schema
--
-- Run this against your MySQL / AWS RDS instance if you prefer to manage the
-- schema yourself. If you leave spring.jpa.hibernate.ddl-auto=update in
-- application.properties, Hibernate creates this table for you on first run
-- and you can skip this file entirely.
-- ---------------------------------------------------------------------------

CREATE DATABASE IF NOT EXISTS loginapp_db;
USE loginapp_db;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL
);

-- Note: `password` stores a BCrypt hash (60 characters), never plain text.
-- The UNIQUE constraints on username/email back up the duplicate checks the
-- application performs at registration time.
