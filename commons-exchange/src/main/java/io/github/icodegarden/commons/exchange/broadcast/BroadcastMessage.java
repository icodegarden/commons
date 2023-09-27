package io.github.icodegarden.commons.exchange.broadcast;

import java.io.Serializable;

import io.github.icodegarden.commons.lang.BodyObject;
import io.github.icodegarden.commons.lang.Matcher;
import io.github.icodegarden.commons.lang.concurrent.registry.Instance;
import lombok.Getter;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface BroadcastMessage extends BodyObject<Object> {

	/**
	 * @return Nullable 
	 */
	Matcher<Instance> instanceMatcher();

	long timeoutMillis();

	@Getter
	class Default implements BroadcastMessage, Serializable {
		private static final long serialVersionUID = 1L;

		private Object body;

		private transient long timeoutMillis;

		private transient Matcher<Instance> instanceMatcher;

		public Default(Object body, long timeoutMillis, Matcher<Instance> instanceMatcher) {
			super();
			this.body = body;
			this.timeoutMillis = timeoutMillis;
			this.instanceMatcher = instanceMatcher;
		}

		@Override
		public Matcher<Instance> instanceMatcher() {
			return instanceMatcher;
		}

		@Override
		public long timeoutMillis() {
			return timeoutMillis;
		}

	}
}
