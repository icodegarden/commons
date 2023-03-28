drop DATABASE IF EXISTS `commons`;
create DATABASE `commons`;
USE `commons`;


-- 消费者系统表
DROP TABLE IF EXISTS `consumer_system`;
CREATE TABLE `consumer_system` (
  `id` bigint unsigned NOT NULL,
  `name` varchar(30) NOT NULL COMMENT '系统名',
  `email` varchar(80) NOT NULL COMMENT '系统管理员邮箱',
  `sasl_username` varchar(30) NOT NULL,
  `sasl_password` varchar(50) NOT NULL,
  `app_id` varchar(80) NOT NULL UNIQUE COMMENT '反查需要',
  `is_actived` bit(1) NOT NULL DEFAULT 1 COMMENT '是否有效',
  `created_by` varchar(32) NOT NULL COMMENT '创建人',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_by` varchar(32) NOT NULL COMMENT '最后修改人',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
