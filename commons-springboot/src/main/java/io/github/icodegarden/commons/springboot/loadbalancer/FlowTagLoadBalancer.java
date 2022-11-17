package io.github.icodegarden.commons.springboot.loadbalancer;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
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
import org.springframework.util.StringUtils;

import reactor.core.publisher.Mono;

/**
 * 负载均衡算法：<br>
 * 流量没有grey_tag的，只选择tag null的服务; <br>
 * 流量有grey_tag的，则优先选择有此tag的服务，其次选择tag
 * null的服务（优先而不是必须找到匹配的原因是：部分服务可能不发版或不需要灰度）<br>
 * <br>
 * 
 * 默认的metadataTagName是tag.flow<br>
 * 默认的flowTagProvider是从request中获取header=X-Flow-Tag的值<br>
 * 默认的L2 LoadBalancer是轮询<br>
 * 
 * @author Fangfang.Xu
 */
public class FlowTagLoadBalancer implements ReactorServiceInstanceLoadBalancer {

	private static final Logger log = LoggerFactory.getLogger(FlowTagLoadBalancer.class);

	public static final String HTTPHEADER_FLOWTAG = "X-Flow-Tag";

	private String metadataTagName = "tag.flow";

	private Function<Request, String> flowTagProvider = new DefaultFlowTagProvider();

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

		String flowTag = flowTagProvider.apply(request);

		List<ServiceInstance> instancesToChoose;

		if (!StringUtils.hasText(flowTag)) {
			instancesToChoose = instances.stream().filter(instance -> {
				String tagValue = instance.getMetadata().get(metadataTagName);
				return !StringUtils.hasText(tagValue);
			}).collect(Collectors.toList());
		} else {
			instancesToChoose = instances.stream().filter(instance -> {
				String tagValue = instance.getMetadata().get(metadataTagName);
				return Objects.equals(flowTag, tagValue);
			}).collect(Collectors.toList());

			if (instancesToChoose.isEmpty()) {
				instancesToChoose = instances.stream().filter(instance -> {
					String tagValue = instance.getMetadata().get(metadataTagName);
					return !StringUtils.hasText(tagValue);
				}).collect(Collectors.toList());
			}
		}

		return l2LoadBalancer.processInstanceResponse(supplier, instancesToChoose);
	}

	public void setMetadataTagName(String metadataTagName) {
		this.metadataTagName = metadataTagName;
	}

	public void setFlowTagProvider(Function<Request, String> flowTagProvider) {
		this.flowTagProvider = flowTagProvider;
	}

	public void setL2LoadBalancer(L2LoadBalancer l2LoadBalancer) {
		this.l2LoadBalancer = l2LoadBalancer;
	}

	private class DefaultFlowTagProvider implements Function<Request, String> {
		@Override
		public String apply(Request request) {
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
			String first = clientRequest.getHeaders().getFirst(HTTPHEADER_FLOWTAG);
			return first;
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
