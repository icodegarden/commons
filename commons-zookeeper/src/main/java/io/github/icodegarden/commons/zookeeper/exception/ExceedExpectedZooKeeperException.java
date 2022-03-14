package io.github.icodegarden.commons.zookeeper.exception;

/**
 * 无法预料的zk异常
 * 
 * @author Fangfang.Xu
 *
 */
public class ExceedExpectedZooKeeperException extends ZooKeeperException {
	private static final long serialVersionUID = 1L;

	public ExceedExpectedZooKeeperException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExceedExpectedZooKeeperException(Throwable cause) {
		super(cause);
	}
}