package io.github.icodegarden.commons.nio.pool;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.commons.lang.exception.remote.RemoteException;
import io.github.icodegarden.commons.lang.util.SystemUtils;
import io.github.icodegarden.commons.lang.util.ThreadUtils;
import io.github.icodegarden.commons.nio.NioClient;

/**
 * 因为最终都会通过getElseSupplier获取连接，而getElseSupplier会识别连接是否已断开（若已断开则从连接池移除），因此不需要NioClient主动维护自己从连接池移除
 * 
 * @author Fangfang.Xu
 *
 */
public class NioClientPool implements Closeable {
	private static final Logger log = LoggerFactory.getLogger(NioClientPool.class);

	private ConcurrentHashMap<String/* ipport */, NioClient> nioClients = new ConcurrentHashMap<String, NioClient>();

	private String poolName;
	private NioClientSupplier defaultSupplier;

	private ScheduledThreadPoolExecutor scheduledThreadPool = ThreadUtils
			.newSingleScheduledThreadPool("NioClientPool-ClearClosedSchedule");
	private ScheduledFuture<?> future;

	public static NioClientPool newPool(String poolName, NioClientSupplier defaultSupplier) {
		return newPool(poolName, defaultSupplier, 60 * 1000);
	}

	public static NioClientPool newPool(String poolName, NioClientSupplier defaultSupplier,
			long clearClosedScheduleMillis) {
		return new NioClientPool(poolName, defaultSupplier, clearClosedScheduleMillis);
	}

	private NioClientPool(String poolName, NioClientSupplier defaultSupplier, long clearClosedScheduleMillis) {
		this.poolName = poolName;
		this.defaultSupplier = defaultSupplier;

		startClearClosedSchedule(clearClosedScheduleMillis);
	}

	private void startClearClosedSchedule(long scheduleMillis) {
		if (future != null) {
			throw new IllegalStateException("schedule was started");
		}
		future = scheduledThreadPool.scheduleWithFixedDelay(() -> {
			for (Entry<String, NioClient> entry : nioClients.entrySet()) {
				removePoolIfClosed(entry.getKey());
			}
		}, scheduleMillis, scheduleMillis, TimeUnit.MILLISECONDS);
	}

	public String getPoolName() {
		return poolName;
	}

	public void setPoolName(String poolName) {
		this.poolName = poolName;
	}

	public NioClient getElseSupplier(InetSocketAddress bind) throws RemoteException {
		return getElseSupplier(bind, defaultSupplier);
	}

	public NioClient getElseSupplier(InetSocketAddress bind, NioClientSupplier supplier) throws RemoteException {
		return getElseSupplier(bind.getHostName(), bind.getPort(), supplier);
	}

	public NioClient getElseSupplier(String ip, int port) throws RemoteException {
		return getElseSupplier(ip, port, defaultSupplier);
	}

	/**
	 * 
	 * @param ip
	 * @param port
	 * @return
	 * @throws NioException on connect failed
	 */
	public NioClient getElseSupplier(String ip, int port, NioClientSupplier supplier) throws RemoteException {
		String ipport = SystemUtils.formatIpPort(ip, port);

		removePoolIfClosed(ipport);

		NioClient nioClient = nioClients.get(ipport);

		if (nioClient == null) {
			nioClient = supplier.get(ip, port);
			if (nioClient.isClosed()) {
				nioClient.connect();
			}
			NioClient pre = nioClients.put(ipport, nioClient);
			if (pre != null) {// 并发put可能
				try {
					pre.close();
				} catch (IOException e) {
					log.warn("close NioClient failed on a new client replace old", e);
				}
			}
		}
		return nioClient;
	}

	/**
	 * 如果client存在且已关闭，则移除
	 * 
	 * @return removed
	 */
	private void removePoolIfClosed(String clientKey) {
		NioClient nioClient = nioClients.get(clientKey);
		if (nioClient != null && nioClient.isClosed()) {
			log.warn("client was closed, remove from pool. client:{}", nioClient);
			NioClient remove = nioClients.remove(clientKey);
			if (remove != null) {// 并发判断
				try {
					remove.close();
				} catch (IOException e) {
					log.warn("ex on close NioClient failed", e);
				}
			}
		}
	}

	@Override
	public void close() throws IOException {
		scheduledThreadPool.shutdown();
		
		nioClients.values().forEach(client->{
			try {
				client.close();
			} catch (IOException e) {
				log.warn("close NioClient failed on close pool. client:{}",client, e);
			}
		});
		
		nioClients.clear();
	}
}