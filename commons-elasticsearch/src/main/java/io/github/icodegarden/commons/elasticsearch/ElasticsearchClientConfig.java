package io.github.icodegarden.commons.elasticsearch;

import org.springframework.util.Assert;

import io.github.icodegarden.commons.lang.Validateable;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Getter
@Setter
@ToString
public class ElasticsearchClientConfig implements Validateable {

	@NonNull
	private String httpHosts;// http://172.22.122.21:9200,http://172.22.122.?:9200

	private String username;
	
	private String password;
	
	private int connectionRequestTimeout = 3000;// default -1 ; this.deadline = leaseTimeout > 0 ?
												// System.currentTimeMillis() + leaseTimeout : Long.MAX_VALUE;
	private int connectTimeout = 1000;// default 1000
	
	private int socketTimeout = 30000;// default 30000
	
	private boolean keepAlive;
	
	private int keepAliveSeconds = 118000;// ES服务端默认应该是120s
	
	private int maxConnPerRoute = 10;// default 2 ,
										// 对同一地址（例如3个节点使用不同ip时，应该会是每个节点最多几个；3个节点使用统一域名时，应该是按一个节点算）,对一个HttpClient对象而言
	private int maxConnTotal = 20;// default 20 , 总和,对一个HttpClient对象而言
	
	private Sniffer sniffer = new Sniffer();

	public ElasticsearchClientConfig() {
	}

	public ElasticsearchClientConfig(String httpHosts) {
		this.httpHosts = httpHosts;
	}
	
	@Override
	public void validate() throws IllegalArgumentException {
		Assert.hasText(httpHosts, "httpHosts must not empty");
	}

	@Getter
	@Setter
	@ToString
	public static class Sniffer {
		private boolean enabled;
		private int sniffIntervalMillis = 60000;/* by default every 5 minutes */
		private int sniffAfterFailureDelayMillis = 30000;
	}
	
}