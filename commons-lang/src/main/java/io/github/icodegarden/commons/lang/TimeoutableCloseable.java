package io.github.icodegarden.commons.lang;

import java.io.IOException;
import java.time.Duration;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface TimeoutableCloseable extends java.io.Closeable {

	void close(long timeoutMillis) throws IOException;

	default void close(Duration timeout) throws IOException {
		close(timeout.toMillis());
	}

}
