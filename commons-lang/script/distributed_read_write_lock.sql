DROP TABLE IF EXISTS `distributed_read_write_lock`;
CREATE TABLE `distributed_read_write_lock` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `identifier` varchar(64),
  `is_read_type` bit(1) NOT NULL,
  `is_locked` bit(1) NOT NULL DEFAULT 0,
  `expire_seconds` bigint NOT NULL DEFAULT 0,
  `lock_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;