DROP TABLE IF EXISTS id_sequence;
CREATE TABLE id_sequence (
  NAME VARCHAR (50) NOT NULL,
  current_value BIGINT NOT NULL,
  increment INT NOT NULL,
PRIMARY KEY (NAME)
) ENGINE = INNODB ;


INSERT INTO id_sequence(name,current_value,increment) VALUES ('GLOBAL', 0, 100);


DROP FUNCTION IF EXISTS `id_seq_currval`;
DELIMITER ;;
CREATE  FUNCTION `id_seq_currval`(seq_name VARCHAR(50)) 
RETURNS BIGINT
    DETERMINISTIC
BEGIN 
        DECLARE retval BIGINT;
        SET retval=-1;  
        SELECT current_value INTO retval 
          FROM id_sequence  WHERE name = seq_name;  
        RETURN retval ; 
END
;;
DELIMITER ;


DROP FUNCTION IF EXISTS `id_seq_nextval`;
DELIMITER ;;
CREATE FUNCTION `id_seq_nextval`(seq_name VARCHAR(50)) RETURNS BIGINT
 
    DETERMINISTIC
BEGIN 
         UPDATE id_sequence  
                 SET current_value = current_value + increment 
                  WHERE name = seq_name;  
         RETURN id_seq_currval(seq_name);  
END
;;
DELIMITER ;


DROP FUNCTION IF EXISTS `id_seq_setval`;
DELIMITER ;;
CREATE FUNCTION `id_seq_setval`(seq_name VARCHAR(50), value INTEGER) 
RETURNS BIGINT
    DETERMINISTIC
BEGIN 
         UPDATE id_sequence  
                   SET current_value = value  
                   WHERE name = seq_name;  
         RETURN id_seq_currval(seq_name);  
END
;;
DELIMITER ;


