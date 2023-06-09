package io.github.icodegarden.commons.lang.util;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.core.env.Environment;
import org.springframework.util.ClassUtils;

import com.sun.management.OperatingSystemMXBean;

import io.github.icodegarden.commons.lang.tuple.Tuple2;
import io.github.icodegarden.commons.lang.tuple.Tuples;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class SystemUtils {

	public static final DateTimeFormatter STANDARD_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	public static final DateTimeFormatter STANDARD_DATETIME_FORMATTER = DateTimeFormatter
			.ofPattern("yyyy-MM-dd HH:mm:ss");
	public static final DateTimeFormatter STANDARD_DATETIMEMS_FORMATTER = DateTimeFormatter
			.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

	public static final String OS_NAME = System.getProperty("os.name");

	private static boolean isLinuxPlatform = false;
	private static boolean isWindowsPlatform = false;

	static {
		if (OS_NAME != null && OS_NAME.toLowerCase().contains("linux")) {
			isLinuxPlatform = true;
		}

		if (OS_NAME != null && OS_NAME.toLowerCase().contains("windows")) {
			isWindowsPlatform = true;
		}
	}

	public static boolean isWindowsPlatform() {
		return isWindowsPlatform;
	}

	public static boolean isLinuxPlatform() {
		return isLinuxPlatform;
	}

	/**
	 * 该接口有一定的耗时
	 * 
	 * @return 本机所处网络中的IP
	 */
	public static String getIp() {
		Tuple2<List<String>, List<String>> tuple2 = getIpv46s();
		List<String> ipv4Result = tuple2.getT1();
		List<String> ipv6Result = tuple2.getT2();

		// prefer ipv4
		if (!ipv4Result.isEmpty()) {
			for (String ip : ipv4Result) {
				if (ip.startsWith("127.0") || ip.startsWith("192.168")) {
					continue;
				}

				return ip;
			}

			return ipv4Result.get(ipv4Result.size() - 1);
		} else if (!ipv6Result.isEmpty()) {
			return ipv6Result.get(0);
		}

		// fallback
		if (!ipv4Result.isEmpty()) {
			return ipv4Result.get(0);
		}

		// no possible
		return null;
	}

	/**
	 * 获取本机所有ipv4地址 该接口有一定的耗时
	 */
	public static List<String> getIpv4s() {
		Tuple2<List<String>, List<String>> tuple2 = getIpv46s();
		return tuple2.getT1();
	}

	/**
	 * 获取本机所有ipv6地址 该接口有一定的耗时
	 */
	public static List<String> getIpv6s() {
		Tuple2<List<String>, List<String>> tuple2 = getIpv46s();
		return tuple2.getT2();
	}

	/**
	 * 获取本机所有ip地址 该接口有一定的耗时
	 */
	public static List<String> getIps() {
		Tuple2<List<String>, List<String>> tuple2 = getIpv46s();
		List<String> t1 = tuple2.getT1();
		t1.addAll(tuple2.getT2());
		return t1;
	}

	/**
	 * 获取本机所有ip地址 该接口有一定的耗时
	 * 
	 * @return ipv4,ipv6
	 * @throws on fail
	 */
	private static Tuple2<List<String>, List<String>> getIpv46s() throws IllegalStateException {
		try {
			// Traversal Network interface to get the first non-loopback and non-private
			// address
			Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
			List<String> ipv4Result = new LinkedList<String>();
			List<String> ipv6Result = new LinkedList<String>();
			while (enumeration.hasMoreElements()) {
				final NetworkInterface networkInterface = enumeration.nextElement();
				final Enumeration<InetAddress> en = networkInterface.getInetAddresses();
				while (en.hasMoreElements()) {
					final InetAddress address = en.nextElement();
					if (!address.isLoopbackAddress()) {
						if (address instanceof Inet6Address) {
							ipv6Result.add(normalizeHostAddress(address));
						} else {
							ipv4Result.add(normalizeHostAddress(address));
						}
					}
				}
			}

			if (ipv4Result.isEmpty()) {
				final InetAddress localHost = InetAddress.getLocalHost();
				String localhost = normalizeHostAddress(localHost);
				ipv4Result.add(localhost);
			}

			return Tuples.of(ipv4Result, ipv6Result);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to obtain local address", e);
		}
	}

	/**
	 * 
	 * @param ip
	 * @param port
	 * @return ip:port
	 */
	public static String formatIpPort(String ip, int port) {
		return ip + ":" + port;
	}

	public static Tuple2<String, Integer> resolveIpPort(String ipport) {
		String[] split = ipport.split(":");

		String ip = split[0];
		int port = Integer.parseInt(split[1]);
		return Tuples.of(ip, port);
	}

	public static String normalizeHostAddress(final InetAddress localHost) {
		if (localHost instanceof Inet6Address) {
			return "[" + localHost.getHostAddress() + "]";
		} else {
			return localHost.getHostAddress();
		}
	}

	public static SocketAddress string2SocketAddress(final String addr) {
		int split = addr.lastIndexOf(":");
		String host = addr.substring(0, split);
		String port = addr.substring(split + 1);
		InetSocketAddress isa = new InetSocketAddress(host, Integer.parseInt(port));
		return isa;
	}

	public static String socketAddress2String(final SocketAddress addr) {
		StringBuilder sb = new StringBuilder();
		InetSocketAddress inetSocketAddress = (InetSocketAddress) addr;
		sb.append(inetSocketAddress.getAddress().getHostAddress());
		sb.append(":");
		sb.append(inetSocketAddress.getPort());
		return sb.toString();
	}

	public static SocketChannel connect(SocketAddress remote) {
		return connect(remote, 1000 * 5);
	}

	public static SocketChannel connect(SocketAddress remote, final int timeoutMillis) {
		SocketChannel sc = null;
		try {
			sc = SocketChannel.open();
			sc.configureBlocking(true);
			sc.socket().setSoLinger(false, -1);
			sc.socket().setTcpNoDelay(true);
			sc.socket().setReceiveBufferSize(1024 * 64);
			sc.socket().setSendBufferSize(1024 * 64);
			sc.socket().connect(remote, timeoutMillis);
			sc.configureBlocking(false);
			return sc;
		} catch (Exception e) {
			if (sc != null) {
				try {
					sc.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}

		return null;
	}

	// ------------------------------------------------------------

	private static VMRuntime vmRuntime;

	static {
		try {
			vmRuntime = new OperatingSystemMXBeanRuntime();
		} catch (Throwable e) {
			vmRuntime = new FallbackRuntime();
		}
	}

	public static VMRuntime getVmRuntime() {
		return vmRuntime;
	}

	public interface VMRuntime {
		/**
		 * @return byte
		 */
		long getTotalPhysicalMemorySize();

		/**
		 * @return byte
		 */
		long getFreePhysicalMemorySize();

		/**
		 * @return byte
		 */
		long getUsedPhysicalMemorySize();

		/**
		 * @return byte
		 */
		long getTotalSwapSpaceSize();

		/**
		 * @return byte
		 */
		long getFreeSwapSpaceSize();

		/**
		 * @return byte
		 */
		long getUsedSwapSpaceSize();

		/**
		 * Xmx
		 *
		 * @return byte
		 */
		long getJvmMaxMemory();

		/**
		 * 已分配的JVM内存
		 *
		 * @return byte
		 */
		long getJvmTotalMemory();

		/**
		 * @return byte
		 */
		long getJvmFreeMemory();

		/**
		 * @return byte
		 */
		long getJvmUsedMemory();

		/**
		 * 系统使用率
		 *
		 * @return 0-1.0
		 */
		double getSystemCpuLoad();

		/**
		 * java进程使用率
		 *
		 * @return 0-1.0
		 */
		double getProcessCpuLoad();

//		WIN10
//		JDK8
//		Xss256
//
//		综合结论：每秒能使用多少个线程进行并发，取决于内存和cpu哪个更容易达到阈值
//
//
//		CPU测试---------------------------------------------------------------------------------------
//		结论
//		每核频率为2.10GHz的工作站
//		每秒并发10000线程，每个线程的业务是创建3000个对象
//		结果在linux 逻辑8核环境下稳定占CPU不超过120%，即每核占不超过15%
//		以此计算1核时占cpu不超过80%时，可每秒并发线程=(10000/1.20)*0.8=6600，生产环境的服务器cpu保守也按此性能计算=6600*1=6600，保险起见性能需要*3倍=6600/3=2200
//		即1核最多可承担2200个线程并发
//
//
//		内存测试---------------------------------------------------------------------------------------
//		结论（带业务4G 2G）：每1000个线程 + 1000个对象占堆内存80M，进程占内存120M
//		用于生产保险起见*3倍计算，堆内和进程内存哪个容易达到阈值计算。
//		例如用户配置2G堆内存线程数约2048/80/3=8500个，4G物理内存线程数约4096/120/3=11300个，最终取8500个
//		例如用户配置2G堆内存线程数约2048/80/3=8500个，2.5G物理内存线程数约2560/120/3=7100个，最终取7100个
		default long maxConcurrentThreadsPerSecond() {
			long totalPhysicalMemoryMB = getVmRuntime().getTotalPhysicalMemorySize() / 1024 / 1024;
			long maxTotalPhysicalMemoryThreads = (totalPhysicalMemoryMB / 120 / 3) * 1000;

			long xmxMemory = getVmRuntime().getJvmMaxMemory() / 1024 / 1024;
			long maxXmxMemoryThreads = (xmxMemory / 80 / 3) * 1000;

			int cpuCores = Runtime.getRuntime().availableProcessors();
			int maxCpuThreads = cpuCores * 3300;

			long max = Math.min(maxTotalPhysicalMemoryThreads, maxXmxMemoryThreads);
			max = Math.min(max, maxCpuThreads);
			return max;
		}
	}

	private static class OperatingSystemMXBeanRuntime implements VMRuntime {
		private final OperatingSystemMXBean systemMXBean = (OperatingSystemMXBean) ManagementFactory
				.getOperatingSystemMXBean();
		private final Runtime runtime = Runtime.getRuntime();

		/**
		 * @return byte
		 */
		public long getTotalPhysicalMemorySize() {
			return systemMXBean.getTotalPhysicalMemorySize();
		}

		/**
		 * @return byte
		 */
		public long getFreePhysicalMemorySize() {
			return systemMXBean.getFreePhysicalMemorySize();
		}

		/**
		 * @return byte
		 */
		public long getUsedPhysicalMemorySize() {
			return systemMXBean.getTotalPhysicalMemorySize() - systemMXBean.getFreePhysicalMemorySize();
		}

		/**
		 * @return byte
		 */
		public long getTotalSwapSpaceSize() {
			return systemMXBean.getTotalSwapSpaceSize();
		}

		/**
		 * @return byte
		 */
		public long getFreeSwapSpaceSize() {
			return systemMXBean.getFreeSwapSpaceSize();
		}

		/**
		 * @return byte
		 */
		public long getUsedSwapSpaceSize() {
			return systemMXBean.getTotalSwapSpaceSize() - systemMXBean.getFreeSwapSpaceSize();
		}

		/**
		 * Xmx
		 *
		 * @return byte
		 */
		public long getJvmMaxMemory() {
			return runtime.maxMemory();
		}

		/**
		 * 已分配的JVM内存
		 *
		 * @return byte
		 */
		public long getJvmTotalMemory() {
			return runtime.totalMemory();
		}

		/**
		 * @return byte
		 */
		public long getJvmFreeMemory() {
			return runtime.freeMemory();
		}

		/**
		 * @return byte
		 */
		public long getJvmUsedMemory() {
			return getJvmMaxMemory() - getJvmFreeMemory();
		}

		/**
		 * 系统使用率，首次获取大约耗时1200ms
		 *
		 * @return 0-1.0
		 */
		public double getSystemCpuLoad() {
			return systemMXBean.getSystemCpuLoad();
		}

		/**
		 * java进程使用率
		 *
		 * @return 0-1.0
		 */
		public double getProcessCpuLoad() {
			return CACHE.processCpuLoad;
		}

		private final Cache CACHE = new Cache(3000);

		private class Cache {
			private double processCpuLoad;

			private Cache(long periodMillis) {
				ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
					Thread thread = new Thread(runnable, "Runtime Cache");
					thread.setDaemon(true);
					return thread;
				});
				scheduler.scheduleAtFixedRate(() -> {
					try {
						/**
						 * 每次耗时约20ms
						 */
						processCpuLoad = systemMXBean.getProcessCpuLoad();
					} catch (Exception ignore) {
					}
				}, periodMillis, periodMillis, TimeUnit.MILLISECONDS);
			}
		}
	}

	private static class FallbackRuntime implements VMRuntime {
		private final Runtime runtime = Runtime.getRuntime();

		/**
		 * @return byte
		 */
		public long getTotalPhysicalMemorySize() {
			return Long.MAX_VALUE;
		}

		/**
		 * @return byte
		 */
		public long getFreePhysicalMemorySize() {
			return Long.MAX_VALUE;
		}

		/**
		 * @return byte
		 */
		public long getUsedPhysicalMemorySize() {
			return 0;
		}

		/**
		 * @return byte
		 */
		public long getTotalSwapSpaceSize() {
			return Long.MAX_VALUE;
		}

		/**
		 * @return byte
		 */
		public long getFreeSwapSpaceSize() {
			return Long.MAX_VALUE;
		}

		/**
		 * @return byte
		 */
		public long getUsedSwapSpaceSize() {
			return 0;
		}

		/**
		 * Xmx
		 *
		 * @return byte
		 */
		public long getJvmMaxMemory() {
			return runtime.maxMemory();
		}

		/**
		 * 已分配的JVM内存
		 *
		 * @return byte
		 */
		public long getJvmTotalMemory() {
			return runtime.totalMemory();
		}

		/**
		 * @return byte
		 */
		public long getJvmFreeMemory() {
			return runtime.freeMemory();
		}

		/**
		 * @return byte
		 */
		public long getJvmUsedMemory() {
			return getJvmMaxMemory() - getJvmFreeMemory();
		}

		/**
		 * 系统使用率，首次获取大约耗时1200ms
		 *
		 * @return 0-1.0
		 */
		public double getSystemCpuLoad() {
			return 0;
		}

		/**
		 * java进程使用率
		 *
		 * @return 0-1.0
		 */
		public double getProcessCpuLoad() {
			return 0;
		}
	}

//	public static void main(String[] args) throws Exception {
//	for(;;) {
//		long start = System.currentTimeMillis();
//		System.out.println(getTotalPhysicalMemorySize()/1024.0/1024.0/1024.0);
//		System.out.println("used:"+(System.currentTimeMillis() - start));
//		System.out.println(getFreePhysicalMemorySize()/1024.0/1024.0/1024.0);
//		System.out.println("used:"+(System.currentTimeMillis() - start));
//		System.out.println(getUsedPhysicalMemorySize()/1024.0/1024.0/1024.0);
//		System.out.println("used:"+(System.currentTimeMillis() - start));
//		System.out.println("---------------------------------");
//		
//		
//		System.out.println(getTotalSwapSpaceSize()/1024/1024/1024);
//		System.out.println("used:"+(System.currentTimeMillis() - start));
//		System.out.println(getFreeSwapSpaceSize()/1024/1024/1024);
//		System.out.println("used:"+(System.currentTimeMillis() - start));
//		System.out.println(getUsedSwapSpaceSize()/1024/1024/1024);
//		System.out.println("used:"+(System.currentTimeMillis() - start));
//		System.out.println("---------------------------------");
//		System.out.println(getJvmMaxMemory()/1024.0/1024.0/1024.0);
//		System.out.println("used:"+(System.currentTimeMillis() - start));
//		System.out.println(getJvmTotalMemory()/1024.0/1024.0/1024.0);
//		System.out.println("used:"+(System.currentTimeMillis() - start));
//		System.out.println(getJvmFreeMemory()/1024.0/1024.0/1024.0);
//		System.out.println("used:"+(System.currentTimeMillis() - start));
//		System.out.println(getJvmUsedMemory()/1024.0/1024.0/1024.0);
//		System.out.println("used:"+(System.currentTimeMillis() - start));
//		System.out.println("---------------------------------");
//		System.out.println(getSystemCpuLoad());
//		System.out.println("used:"+(System.currentTimeMillis() - start));
//		System.out.println(getProcessCpuLoad());
//		System.out.println("used:"+(System.currentTimeMillis() - start));
//		System.out.println("---------------------------------");
//		
//		Thread.sleep(3000);
//		System.out.println(".............................");
//	}
//
//	new Thread() {
//		public void run() {
//			for(;;) {
//				System.out.println(getJvmMaxMemory()/1024.0/1024.0/1024.0);
//				System.out.println(getJvmTotalMemory()/1024.0/1024.0/1024.0);
//				System.out.println(getJvmFreeMemory()/1024.0/1024.0/1024.0);
//				System.out.println(getJvmUsedMemory()/1024.0/1024.0/1024.0);
//				System.out.println("---------------------------------");
//				System.out.println(getSystemCpuLoad());
//				System.out.println(getProcessCpuLoad());
//				System.out.println("---------------------------------");
//				try {
//					Thread.sleep(3000);
//				} catch (InterruptedException e) {
//				}
//			}
//		};
//	}.start();
//	
//	ArrayList<Object> arrayList = new ArrayList<>();
//	for(;;) {
//		System.in.read();
//		arrayList.add(new byte[1024*1024]);
//		System.out.println("add...");
//	}
//	
//	for(;;) {
//		System.in.read();
//		new Thread() {
//			public void run() {
//				for(;;) {
//					
//				}
//			};
//		}.start();
//	}
//}

	// ------------------------------------------------------------

	public static LocalDateTime now() {
		return SystemClock.now();
	}

	/**
	 * 因为LocalDateTime的now方法耗时较大，高并发时可以优化
	 * 
	 * @author Fangfang.Xu
	 *
	 */
	private static class SystemClock {

		private final long period;
		private LocalDateTime now;

		private SystemClock(long period) {
			this.period = period;
			this.now = LocalDateTime.now();
			scheduleClockUpdating();
		}

		private static SystemClock instance() {
			return InstanceHolder.INSTANCE;
		}

		public static LocalDateTime now() {
			return instance().now;
		}

		private void scheduleClockUpdating() {
			ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
				Thread thread = new Thread(runnable, SystemClock.class.getSimpleName());
				thread.setDaemon(true);
				return thread;
			});
			scheduler.scheduleWithFixedDelay(() -> {
				this.now = LocalDateTime.now();
			}, period, period, TimeUnit.MILLISECONDS);
		}

		private static class InstanceHolder {
			public static final SystemClock INSTANCE = new SystemClock(10);
		}
	}

	// ------------------------------------------------------------

	@Slf4j
	public static class Server {

		private static final String DEFAULT_SERVER_NAME = "NotConfig";

		private static String serverName = DEFAULT_SERVER_NAME;
		private static String ip;

		static {
			if (ClassUtils.isPresent("io.github.icodegarden.commons.springboot.SpringContext",
					ClassUtils.getDefaultClassLoader())) {
				new Thread() {
					public void run() {
						ThreadUtils.sleep(30000);

						long start = System.currentTimeMillis();
						while (DEFAULT_SERVER_NAME.equals(Server.serverName)
								&& (System.currentTimeMillis() - start < 1800 * 1000)) {
							try {
								Class<?> springContextClass = ClassUtils.forName(
										"io.github.icodegarden.commons.springboot.SpringContext",
										ClassUtils.getDefaultClassLoader());
								Method getApplicationContextMethod = springContextClass
										.getDeclaredMethod("getApplicationContext");
								Object applicationContext = getApplicationContextMethod.invoke(null);
								Class<?> applicationContextClass = ClassUtils.forName(
										"org.springframework.context.ApplicationContext",
										ClassUtils.getDefaultClassLoader());
								Method getBeanMethod = applicationContextClass.getMethod("getBean", Class.class);
								Environment env = (Environment) getBeanMethod.invoke(applicationContext,
										Environment.class);
								String applicationName = env.getRequiredProperty("spring.application.name");

								Server.configServerName(applicationName);
							} catch (Exception e) {
								log.warn("failed on init configServerName");
							}

							ThreadUtils.sleep(30000);
						}
					};
				}.start();
			}
		}

		public static void configServerName(String serverName) {
			Server.serverName = serverName;
		}

		public static String getServerName() {
			return serverName;
		}

		public static String getIp() {
			if (ip != null) {
				return ip;
			}
			return ip = SystemUtils.getIp();
		}
	}

	// ------------------------------------------------------------
}