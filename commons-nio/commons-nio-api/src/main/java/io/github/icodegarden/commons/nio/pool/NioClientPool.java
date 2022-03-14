package io.github.icodegarden.commons.nio.pool;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.commons.lang.exception.remote.RemoteException;
import io.github.icodegarden.commons.lang.util.SystemUtils;
import io.github.icodegarden.commons.nio.NioClient;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class NioClientPool {
	private static final Logger log = LoggerFactory.getLogger(NioClientPool.class);

	private ConcurrentHashMap<String/* ipport */, NioClient> nioClients = new ConcurrentHashMap<String, NioClient>();

	private String poolName;
	private NioClientSupplier defaultSupplier;

	public static NioClientPool newPool(String poolName, NioClientSupplier defaultSupplier) {
		return new NioClientPool(poolName, defaultSupplier);
	}

	private NioClientPool(String poolName, NioClientSupplier defaultSupplier) {
		this.poolName = poolName;
		this.defaultSupplier = defaultSupplier;
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

		NioClient nioClient = nioClients.get(ipport);

		if (nioClient != null) {
			if (nioClient.isClosed()) {
				NioClient remove = nioClients.remove(ipport);
				if (remove != null) {
					try {
						remove.close();
					} catch (IOException e) {
						log.warn("ex on close NioClient failed", e);
					}
				}

				nioClient = null;
			}
		}

		if (nioClient == null) {
			nioClient = supplier.get(ip, port);
			if (nioClient.isClosed()) {
				nioClient.connect();
			}
			NioClient pre = nioClients.put(ipport, nioClient);
			if (pre != null) {
				try {
					pre.close();
				} catch (IOException e) {
					log.warn("close NioClient failed on a new client replace old", e);
				}
			}
		}
		return nioClient;
	}
}