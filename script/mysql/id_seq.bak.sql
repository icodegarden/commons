DROP TABLE IF EXISTS id_sequence;
CREATE TABLE id_sequence (
NAME VARCHAR (50) NOT NULL,
current_value INT NOT NULL,
increment INT NOT NULL DEFAULT 100,
PRIMARY KEY (NAME)
) ENGINE = INNODB ;


INSERT INTO id_sequence(name,current_value,increment) VALUES ('GLOBAL', 100000, 100);


DROP FUNCTION IF EXISTS `id_seq_currval`;
DELIMITER ;;
CREATE  FUNCTION `id_seq_currval`(seq_name VARCHAR(50)) 
RETURNS varchar(64) CHARSET utf8
    DETERMINISTIC
BEGIN 
        DECLARE retval VARCHAR(64);
        SET retval="-999999999,null";  
        SELECT concat(CAST(current_value AS CHAR),",",CAST(increment AS CHAR) ) INTO retval 
          FROM id_sequence  WHERE name = seq_name;  
        RETURN retval ; 
END
;;
DELIMITER ;


DROP FUNCTION IF EXISTS `id_seq_nextval`;
DELIMITER ;;
CREATE FUNCTION `id_seq_nextval`(seq_name VARCHAR(50)) RETURNS varchar(64)
 CHARSET utf8
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
RETURNS varchar(64) CHARSET utf8
    DETERMINISTIC
BEGIN 
         UPDATE id_sequence  
                   SET current_value = value  
                   WHERE name = seq_name;  
         RETURN id_seq_currval(seq_name);  
END
;;
DELIMITER ;
