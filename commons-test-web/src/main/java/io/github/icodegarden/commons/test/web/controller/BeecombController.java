package io.github.icodegarden.commons.test.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.icodegarden.beecomb.client.BeeCombClient;
import io.github.icodegarden.beecomb.client.pojo.transfer.CreateDelayJobDTO;
import io.github.icodegarden.beecomb.client.pojo.transfer.CreateDelayJobDTO.Delay;
import io.github.icodegarden.beecomb.client.pojo.view.CreateJobVO;
import io.github.icodegarden.commons.test.web.service.QuickStartJobHandler;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@RestController
public class BeecombController {

	@Autowired
	BeeCombClient beeCombClient;
	@Autowired
	Environment env;

	@GetMapping("beecomb/create")
	public ResponseEntity<?> create() {
		String EXECUTOR_NAME = env.getProperty("spring.application.name");

		Delay delay = new CreateDelayJobDTO.Delay(3000L);
		delay.setRetryOnNoQualified(3);
		delay.setRetryOnExecuteFailed(3);
		CreateDelayJobDTO job = new CreateDelayJobDTO("QuickStartDelayJob", EXECUTOR_NAME, QuickStartJobHandler.NAME,
				delay);
		CreateJobVO response = beeCombClient.createJob(job);

		if (response.getDispatchException() == null) {
			System.out.println("创建示例任务成功，队列所在实例：" + response.getJob().getQueuedAtInstance()/* 若使用async方式，则该字段是null */);
		} else {
			System.out.println("创建示例任务成功，但分配队列失败：" + response.getDispatchException());
		}

		return ResponseEntity.ok(response);
	}
}
