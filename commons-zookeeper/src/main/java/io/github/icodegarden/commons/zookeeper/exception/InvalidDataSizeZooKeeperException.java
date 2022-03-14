package io.github.icodegarden.commons.zookeeper.exception;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class InvalidDataSizeZooKeeperException extends ZooKeeperException {
	private static final long serialVersionUID = 1L;

	public static final int MAX_DATA_SIZE = 1024 * 1024;// 1M

	public InvalidDataSizeZooKeeperException(int size) {
		super(String.format("data size was gte [%d], current is [%d]", MAX_DATA_SIZE, size));
	}
}