-- PuppyTalk Database Initialization Script

-- Create development database if not exists
CREATE DATABASE IF NOT EXISTS puppytalk_dev;

-- Create test database if not exists  
CREATE DATABASE IF NOT EXISTS puppytalk_test;

-- Grant privileges
GRANT ALL PRIVILEGES ON puppytalk.* TO 'puppytalk'@'%';
GRANT ALL PRIVILEGES ON puppytalk_dev.* TO 'puppytalk'@'%';
GRANT ALL PRIVILEGES ON puppytalk_test.* TO 'puppytalk'@'%';

FLUSH PRIVILEGES;