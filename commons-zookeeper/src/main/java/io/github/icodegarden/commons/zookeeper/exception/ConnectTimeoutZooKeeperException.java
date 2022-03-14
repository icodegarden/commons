package io.github.icodegarden.commons.zookeeper.exception;

/**
 * 无法预料的zk异常
 * 
 * @author Fangfang.Xu
 *
 */
public class ConnectTimeoutZooKeeperException extends ZooKeeperException {
	private static final long serialVersionUID = 1L;

	public ConnectTimeoutZooKeeperException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConnectTimeoutZooKeeperException(Throwable cause) {
		super(cause);
	}
	
	public ConnectTimeoutZooKeeperException(String message) {
		super(message);
	}
}