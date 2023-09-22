DROP TABLE IF EXISTS `registry`;
CREATE TABLE `registry` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(30) NOT NULL COMMENT '例如服务名',
  `identifier` varchar(64) COMMENT '唯一身份识别例如ip:port',
  `index` int COMMENT '序号',
  `is_registered` bit(1) NOT NULL DEFAULT 0,
  `metadata` JSON,
  `info` JSON,
  `expire_seconds` bigint NOT NULL DEFAULT 0,
  `lease_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX idx_name(`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
