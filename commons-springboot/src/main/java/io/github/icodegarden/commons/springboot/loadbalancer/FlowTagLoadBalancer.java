package io.github.icodegarden.commons.springboot.loadbalancer;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultRequestContext;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.RequestData;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.SelectedInstanceCallback;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import io.github.icodegarden.commons.lang.util.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import reactor.core.publisher.Mono;

/**
 * 负载均衡算法：<br>
 * 流量有X-FlowTag-Required的，必须完全匹配有此tag的实例<br>
 * 流量有X-FlowTag-First的，优先选择有此tag的服务，其次选择没有任何tag的实例（优先而不是必须找到匹配的原因是：部分服务可能不发版或不需要灰度）<br>
 * 流量没有任何tag的，只选择没有任何tag的实例<br>
 * 
 * <br>
 * 
 * 默认的instanceMetadataTagName是flow.tags, json array, ["a","b",...]<br>
 * 默认的IdentityFlowTagExtractor是从request.header中获取X-FlowTag-Required、X-FlowTag-First的值<br>
 * 默认的L2 LoadBalancer是轮询<br>
 * 
 * @author Fangfang.Xu
 */
public class FlowTagLoadBalancer implements ReactorServiceInstanceLoadBalancer {

	private static final Logger log = LoggerFactory.getLogger(FlowTagLoadBalancer.class);

	public static final String HTTPHEADER_FLOWTAG_REQUIRED = "X-FlowTag-Required";
	public static final String HTTPHEADER_FLOWTAG_FIRST = "X-FlowTag-First";

	/**
	 * json array, ["a","b",...]
	 */
	private String instanceMetadataTagName = "flow.tags";

	private IdentityFlowTagExtractor identityFlowTagExtractor = new DefaultIdentityFlowTagExtractor();

	private L2LoadBalancer l2LoadBalancer = new RoundRobinLoadBalancer();

	private final String serviceId;

	private ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;

	public FlowTagLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider,
			String serviceId) {
		this.serviceId = serviceId;
		this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
	}

	@Override
	public Mono<Response<ServiceInstance>> choose(Request request) {
		ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider
				.getIfAvailable(NoopServiceInstanceListSupplier::new);
		return supplier.get().next().map(serviceInstances -> getInstanceResponse(request, supplier, serviceInstances));
	}

	private Response<ServiceInstance> getInstanceResponse(Request request, ServiceInstanceListSupplier supplier,
			List<ServiceInstance> instances) {
		if (instances.isEmpty()) {
			if (log.isWarnEnabled()) {
				log.warn("No servers available for service: " + serviceId);
			}
			return new EmptyResponse();
		}

		IdentityFlowTag identityFlowTag = identityFlowTagExtractor.extract(request);

		List<ServiceInstance> instancesToChoose;

		if (StringUtils.hasText(identityFlowTag.getFlowTagRequired())) {
			instancesToChoose = instances.stream().filter(instance -> {
				String tagValue = instance.getMetadata().get(instanceMetadataTagName);
				if (!StringUtils.hasText(tagValue)) {
					return false;
				}
				List<String> tags = JsonUtils.deserializeArray(tagValue, String.class);
				return tags.contains(identityFlowTag.getFlowTagRequired());
			}).collect(Collectors.toList());
		} else if (StringUtils.hasText(identityFlowTag.getFlowTagFirst())) {
			instancesToChoose = instances.stream().filter(instance -> {
				String tagValue = instance.getMetadata().get(instanceMetadataTagName);
				if (!StringUtils.hasText(tagValue)) {
					return false;
				}
				List<String> tags = JsonUtils.deserializeArray(tagValue, String.class);
				return tags.contains(identityFlowTag.getFlowTagFirst());
			}).collect(Collectors.toList());

			if (instancesToChoose.isEmpty()) {
				instancesToChoose = filteredInstancesNonFlowTags(instances);
			}
		} else {
			instancesToChoose = filteredInstancesNonFlowTags(instances);
		}

		return l2LoadBalancer.processInstanceResponse(supplier, instancesToChoose);
	}

	/**
	 * 没有tag的实例
	 */
	private List<ServiceInstance> filteredInstancesNonFlowTags(List<ServiceInstance> instances) {
		return instances.stream().filter(instance -> {
			String tagValue = instance.getMetadata().get(instanceMetadataTagName);
			return !StringUtils.hasText(tagValue);
		}).collect(Collectors.toList());
	}

	public void setInstanceMetadataTagName(String instanceMetadataTagName) {
		this.instanceMetadataTagName = instanceMetadataTagName;
	}

	public void setIdentityFlowTagExtractor(IdentityFlowTagExtractor identityFlowTagExtractor) {
		this.identityFlowTagExtractor = identityFlowTagExtractor;
	}

	public void setL2LoadBalancer(L2LoadBalancer l2LoadBalancer) {
		this.l2LoadBalancer = l2LoadBalancer;
	}

	public static interface IdentityFlowTagExtractor {

		/**
		 * @return 不为null
		 */
		IdentityFlowTag extract(Request request);
	}

	@NoArgsConstructor
	@AllArgsConstructor
	@Getter
	@Setter
	@ToString
	public static class IdentityFlowTag {
		@Nullable
		private String flowTagRequired;
		@Nullable
		private String flowTagFirst;
	}

	private class DefaultIdentityFlowTagExtractor implements IdentityFlowTagExtractor {
		@Override
		public IdentityFlowTag extract(Request request) {
			Object ctx = request.getContext();
			if (!(ctx instanceof DefaultRequestContext)) {
				if (log.isWarnEnabled()) {
					log.warn("request.context is not a DefaultRequestContext on get flow tag, context is:{}",
							ctx.getClass());
				}
				return null;
			}
			DefaultRequestContext context = (DefaultRequestContext) ctx;
			Object cr = context.getClientRequest();
			if (!(cr instanceof RequestData)) {
				if (log.isWarnEnabled()) {
					log.warn("context.clientRequest is not a RequestData on get flow tag, clientRequest is:{}",
							cr.getClass());
				}
				return null;
			}
			RequestData clientRequest = (RequestData) cr;
			String flowTagRequired = clientRequest.getHeaders().getFirst(HTTPHEADER_FLOWTAG_REQUIRED);
			String flowTagFirst = clientRequest.getHeaders().getFirst(HTTPHEADER_FLOWTAG_FIRST);
			return new IdentityFlowTag(flowTagRequired, flowTagFirst);
		}
	}

	public static interface L2LoadBalancer {
		Response<ServiceInstance> processInstanceResponse(ServiceInstanceListSupplier supplier,
				List<ServiceInstance> serviceInstances);
	}

	public class RoundRobinLoadBalancer implements L2LoadBalancer {

		private final AtomicInteger position = new AtomicInteger(new Random().nextInt(1000));

		public Response<ServiceInstance> processInstanceResponse(ServiceInstanceListSupplier supplier,
				List<ServiceInstance> serviceInstances) {
			Response<ServiceInstance> serviceInstanceResponse = getInstanceResponse(serviceInstances);
			if (supplier instanceof SelectedInstanceCallback && serviceInstanceResponse.hasServer()) {
				((SelectedInstanceCallback) supplier).selectedServiceInstance(serviceInstanceResponse.getServer());
			}
			return serviceInstanceResponse;
		}

		private Response<ServiceInstance> getInstanceResponse(List<ServiceInstance> instances) {
			if (instances.isEmpty()) {
				if (log.isWarnEnabled()) {
					log.warn("No servers available for service: " + serviceId);
				}
				return new EmptyResponse();
			}

			// Ignore the sign bit, this allows pos to loop sequentially from 0 to
			// Integer.MAX_VALUE
			int pos = this.position.incrementAndGet() & Integer.MAX_VALUE;

			ServiceInstance instance = instances.get(pos % instances.size());

			return new DefaultResponse(instance);
		}
	}

	public class RandomLoadBalancer implements L2LoadBalancer {

		public Response<ServiceInstance> processInstanceResponse(ServiceInstanceListSupplier supplier,
				List<ServiceInstance> serviceInstances) {
			Response<ServiceInstance> serviceInstanceResponse = getInstanceResponse(serviceInstances);
			if (supplier instanceof SelectedInstanceCallback && serviceInstanceResponse.hasServer()) {
				((SelectedInstanceCallback) supplier).selectedServiceInstance(serviceInstanceResponse.getServer());
			}
			return serviceInstanceResponse;
		}

		private Response<ServiceInstance> getInstanceResponse(List<ServiceInstance> instances) {
			if (instances.isEmpty()) {
				if (log.isWarnEnabled()) {
					log.warn("No servers available for service: " + serviceId);
				}
				return new EmptyResponse();
			}
			int index = ThreadLocalRandom.current().nextInt(instances.size());

			ServiceInstance instance = instances.get(index);

			return new DefaultResponse(instance);
		}
	}

}
