package io.github.icodegarden.commons.test.web.service;

import org.springframework.stereotype.Service;

import io.github.icodegarden.beecomb.common.executor.DelayJob;
import io.github.icodegarden.beecomb.common.executor.ExecuteJobResult;
import io.github.icodegarden.beecomb.common.executor.Job;
import io.github.icodegarden.beecomb.executor.registry.JobHandler;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Service
public class QuickStartJobHandler implements JobHandler {
	public static final String NAME = "QuickStartAppJobHandler";

	@Override
	public String name() {
		return NAME;
	}

	@Override
	public ExecuteJobResult handle(Job job) throws Exception {
		System.out.println("handle job:" + job);

		if (job instanceof DelayJob) {
			long delay = ((DelayJob) job).getDelay();
			System.out.println(delay);
		}

		return new ExecuteJobResult();// 执行成功
	}
}