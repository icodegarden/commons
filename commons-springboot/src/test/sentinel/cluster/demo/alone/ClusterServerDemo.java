package cluster.demo.alone;

import java.util.Collections;
import java.util.List;

import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterParamFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.server.ClusterTokenServer;
import com.alibaba.csp.sentinel.cluster.server.SentinelDefaultTokenServer;
import com.alibaba.csp.sentinel.cluster.server.config.ClusterServerConfigManager;
import com.alibaba.csp.sentinel.cluster.server.config.ServerTransportConfig;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.fastjson.JSON;


/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ClusterServerDemo {

	private final static String remoteAddress = "172.22.122.27:8848";
	private final static String groupId = "Sentinel";

	public static void main(String[] args) throws Exception {
		System.setProperty("server.port", "8720");
		System.setProperty("project.name", "cluster-server-alone");
		System.setProperty("csp.sentinel.dashboard.server", "localhost:8858");
//		System.setProperty("csp.sentinel.api.port", "8788");
		
		init();

		// Not embedded mode by default (alone mode).
		ClusterTokenServer tokenServer = new SentinelDefaultTokenServer();

		// A sample for manually load config for cluster server.
		// It's recommended to use dynamic data source to cluster manage config and
		// rules.
		// See the sample in DemoClusterServerInitFunc for detail.
		ClusterServerConfigManager
				.loadGlobalTransportConfig(new ServerTransportConfig().setIdleSeconds(600).setPort(11111));
		ClusterServerConfigManager.loadServerNamespaceSet(Collections.singleton("gddc-manage"));

		// Start the server.
		tokenServer.start();
	}

	public static void init() throws Exception {
		// Register cluster flow rule property supplier which creates data source by
		// namespace.
		ClusterFlowRuleManager.setPropertySupplier(namespace -> {
			ReadableDataSource<String, List<FlowRule>> ds = new NacosDataSource<>(remoteAddress, groupId,
					namespace + "", source -> {
						SentinelProperties deserialize = JSON.parseObject(source, SentinelProperties.class);
						System.out.println("flows:" + deserialize.getFlows());
						return deserialize.getFlows();
					});
			return ds.getProperty();
		});
		// Register cluster parameter flow rule property supplier.
		ClusterParamFlowRuleManager.setPropertySupplier(namespace -> {
			ReadableDataSource<String, List<ParamFlowRule>> ds = new NacosDataSource<>(remoteAddress, groupId,
					namespace + "", source -> {
						SentinelProperties deserialize = JSON.parseObject(source, SentinelProperties.class);
						System.out.println("paramflows:" + deserialize.getParamFlows());
						return deserialize.getParamFlows();
					});
			return ds.getProperty();
		});

		// Server namespace set (scope) data source.
//        ReadableDataSource<String, Set<String>> namespaceDs = new NacosDataSource<>(remoteAddress, groupId,
//            namespaceSetDataId, source -> JSON.parseObject(source, new TypeReference<Set<String>>() {}));
//        ClusterServerConfigManager.registerNamespaceSetProperty(namespaceDs.getProperty());

		// Server transport configuration data source.
//        ReadableDataSource<String, ServerTransportConfig> transportConfigDs = new NacosDataSource<>(remoteAddress,
//            groupId, serverTransportDataId,
//            source -> JSON.parseObject(source, new TypeReference<ServerTransportConfig>() {}));
//        ClusterServerConfigManager.registerServerTransportProperty(transportConfigDs.getProperty());
	}
}
