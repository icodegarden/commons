package io.github.icodegarden.commons.kafka;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface RecordExecutor {

	void execute(ConsumerRecordTask command);
	
	void shutdown();

	public static class ThreadPoolHandleRecordExecutor implements RecordExecutor {

		private final ThreadPoolExecutor threadPoolExecutor;

		public ThreadPoolHandleRecordExecutor(ThreadPoolExecutor threadPoolExecutor) {
			super();
			this.threadPoolExecutor = threadPoolExecutor;
		}

		@Override
		public void execute(ConsumerRecordTask command) {
			threadPoolExecutor.execute(command);
		}
		
		@Override
		public void shutdown() {
			threadPoolExecutor.shutdown();
		}

	}

	public static class CallerRunsExecutor implements RecordExecutor {

		@Override
		public void execute(ConsumerRecordTask command) {
			command.run();
		}
		@Override
		public void shutdown() {
		}
	}
}
