package io.github.icodegarden.commons.springboot.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.github.icodegarden.beecomb.common.properties.ZooKeeper;
import io.github.icodegarden.beecomb.executor.ZooKeeperSupportInstanceProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@ConfigurationProperties(prefix = "commons.beecomb.executor")
@Getter
@Setter
@ToString(callSuper = true)
public class CommonsBeeCombExecutorProperties extends ZooKeeperSupportInstanceProperties {

	public CommonsBeeCombExecutorProperties() {
		super(new ZooKeeper());
	}

	public CommonsBeeCombExecutorProperties(ZooKeeper zookeeper) {
		super(zookeeper);
	}

}