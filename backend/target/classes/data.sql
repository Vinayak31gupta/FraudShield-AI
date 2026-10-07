-- ====================================================================
-- FraudShield AI - Seed Data (BCrypt hashed passwords)
-- Admin@123 -> $2a$10$wT8m9M3.P3m7F36m3Y6u0ee9kH/g/hOskQe5H5wQe8jEevv.1f0G2
-- User@123  -> $2a$10$eO1d4l6t8bK6v8Q9z5uGoeF9o4/0aN6k5oK5sM1eD9e9v8t7u6y5G
-- ====================================================================

-- Users
INSERT IGNORE INTO `users` (`id`, `email`, `password`, `full_name`, `role`, `enabled`, `created_at`) VALUES
(1, 'admin@fraudshield.ai', '$2a$10$wT8m9M3.P3m7F36m3Y6u0ee9kH/g/hOskQe5H5wQe8jEevv.1f0G2', 'Chief Security Officer', 'ROLE_ADMIN', TRUE, NOW()),
(2, 'john.doe@example.com', '$2a$10$eO1d4l6t8bK6v8Q9z5uGoeF9o4/0aN6k5oK5sM1eD9e9v8t7u6y5G', 'John Doe', 'ROLE_USER', TRUE, NOW());
