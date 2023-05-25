package io.github.icodegarden.commons.springboot.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.github.icodegarden.commons.shardingsphere.builder.DataSourceOnlyConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@ConfigurationProperties(prefix = "commons.shardingsphere")
@Getter
@Setter
@ToString
public class CommonsShardingSphereProperties extends DataSourceOnlyConfig {

}