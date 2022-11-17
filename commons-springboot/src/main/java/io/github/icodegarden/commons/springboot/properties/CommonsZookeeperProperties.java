package io.github.icodegarden.commons.springboot.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.github.icodegarden.commons.zookeeper.ZooKeeperHolder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@ConfigurationProperties(prefix = "commons.zookeeper")
@Getter
@Setter
@ToString(callSuper = true)
public class CommonsZookeeperProperties extends ZooKeeperHolder.Config {

}