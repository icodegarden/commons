DROP TABLE IF EXISTS `table_data_count`;
CREATE TABLE `table_data_count` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `table_name` varchar(50) NOT NULL UNIQUE,
  `data_count` bigint NOT NULL DEFAULT 0,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;