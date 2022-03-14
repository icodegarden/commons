package io.github.icodegarden.commons.lang.registry;

import io.github.icodegarden.commons.lang.NamedObject;
import io.github.icodegarden.commons.lang.annotation.Nullable;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface RegisteredInstance extends NamedObject {

	String getServiceName();

	String getInstanceName();

	/**
	 * 
	 * @return 例如http,https
	 */
	@Nullable
	default String getScheme() {
		return null;
	}

	String getIp();

	int getPort();

	public static class Default implements RegisteredInstance {

		private String serviceName;

		private String instanceName;

		private String scheme;

		private String ip;

		private int port;

		public Default(String serviceName, String instanceName, String ip, int port) {
			this(serviceName, instanceName, null, ip, port);
		}
		
		public Default(String serviceName, String instanceName, @Nullable String scheme, String ip, int port) {
			this.serviceName = serviceName;
			this.instanceName = instanceName;
			this.scheme = scheme;
			this.ip = ip;
			this.port = port;
		}

		@Override
		public String name() {
			return getServiceName();
		}

		public String getServiceName() {
			return serviceName;
		}

		public String getInstanceName() {
			return instanceName;
		}

		@Override
		public String getScheme() {
			return scheme;
		}

		public String getIp() {
			return ip;
		}

		public int getPort() {
			return port;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((instanceName == null) ? 0 : instanceName.hashCode());
			result = prime * result + ((ip == null) ? 0 : ip.hashCode());
			result = prime * result + port;
			result = prime * result + ((scheme == null) ? 0 : scheme.hashCode());
			result = prime * result + ((serviceName == null) ? 0 : serviceName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Default other = (Default) obj;
			if (instanceName == null) {
				if (other.instanceName != null)
					return false;
			} else if (!instanceName.equals(other.instanceName))
				return false;
			if (ip == null) {
				if (other.ip != null)
					return false;
			} else if (!ip.equals(other.ip))
				return false;
			if (port != other.port)
				return false;
			if (scheme == null) {
				if (other.scheme != null)
					return false;
			} else if (!scheme.equals(other.scheme))
				return false;
			if (serviceName == null) {
				if (other.serviceName != null)
					return false;
			} else if (!serviceName.equals(other.serviceName))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Default [serviceName=" + serviceName + ", instanceName=" + instanceName + ", scheme=" + scheme
					+ ", ip=" + ip + ", port=" + port + "]";
		}

	}
}
