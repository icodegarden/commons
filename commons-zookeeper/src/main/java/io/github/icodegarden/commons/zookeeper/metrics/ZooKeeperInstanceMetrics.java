package io.github.icodegarden.commons.zookeeper.metrics;

import io.github.icodegarden.commons.lang.metrics.InstanceMetrics;
import io.github.icodegarden.commons.lang.metrics.Metrics;

/**
 * ZooKeeperInstanceMetrics属于ZooKeeper系列的InstanceMetrics，因此是个接口而不是实现类，以便应对可能的扩展需求
 * 
 * @author Fangfang.Xu
 *
 */
public interface ZooKeeperInstanceMetrics<M extends Metrics> extends InstanceMetrics<M> {

}
