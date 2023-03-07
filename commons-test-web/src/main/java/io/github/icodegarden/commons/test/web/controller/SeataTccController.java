package io.github.icodegarden.commons.test.web.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.icodegarden.commons.lang.util.SystemUtils;
import io.github.icodegarden.commons.test.web.feign.SelfFeign;
import io.github.icodegarden.commons.test.web.mapper.ConsumerSystemMapper;
import io.github.icodegarden.commons.test.web.pojo.persistence.ConsumerSystemPO;
import io.seata.core.context.RootContext;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;
import io.seata.spring.annotation.GlobalTransactional;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@RestController
public class SeataTccController {

	@Autowired
	private TccService tccService;

	@GlobalTransactional//TCC模式时必须放在@LocalTCC Service的外面才起作用
	@GetMapping("seata/tcc")
	public ResponseEntity<?> tcc() throws Exception {
		ConsumerSystemPO po = tccService.tcc1(System.currentTimeMillis(), "1-"+System.currentTimeMillis());

		return ResponseEntity.ok(po);
	}

	@GetMapping("feign/tcc")
	public ResponseEntity<?> feignTCC() throws Exception {
		ConsumerSystemPO po = tccService.tcc2(System.currentTimeMillis(), "2-"+System.currentTimeMillis());
		
		return ResponseEntity.ok(po);
	}
	
	@LocalTCC//@GlobalTransactional在本地，且本地也需要事务，则需要加@LocalTCC（可以在接口上）
	@Service
	public class TccService {
		@Autowired
		private ConsumerSystemMapper consumerSystemMapper;
		@Autowired
		private SelfFeign selfFeign;
		
		@TwoPhaseBusinessAction(name = "tcc1", commitMethod = "tcc1commit", rollbackMethod = "tcc1rollback", useTCCFence = false)//可以在接口上
//		@Transactional
		public ConsumerSystemPO tcc1(@BusinessActionContextParameter(paramName = "id") Long id,
				@BusinessActionContextParameter(paramName = "name") String name) throws Exception {
			selfFeign.feignTCC();

			System.out.println("request seata TCC, xid:" + RootContext.getXID());

			ConsumerSystemPO po = new ConsumerSystemPO();
			po.setId(id);
			po.setName(name);
			po.setAppId(System.currentTimeMillis() + "");
			po.setEmail("e");
			po.setSaslPassword("aaa");
			po.setSaslUsername("aaa");

			po.setCreatedBy("xff");
			po.setCreatedAt(SystemUtils.now());
			po.setUpdatedBy("xff");
			po.setUpdatedAt(po.getUpdatedAt());
			consumerSystemMapper.add(po);

//			Thread.sleep(10000);

			int i=1/0;
			return po;
		}

		public void tcc1commit(BusinessActionContext context) {
			System.out.println("tcc1commit");
			
			Map<String, Object> actionContext = context.getActionContext();
			System.out.println(actionContext);
		}

		public void tcc1rollback(BusinessActionContext context) {
			System.out.println("tcc1rollback");
			
			Map<String, Object> actionContext = context.getActionContext();
			System.out.println(actionContext);
			
			Object id = actionContext.get("id");
			consumerSystemMapper.delete((Long)id);
		}

		@TwoPhaseBusinessAction(name = "tcc2", commitMethod = "tcc2commit", rollbackMethod = "tcc2rollback", useTCCFence = false)
//		@Transactional
		public ConsumerSystemPO tcc2(@BusinessActionContextParameter(paramName = "id") Long id,
				@BusinessActionContextParameter(paramName = "name") String name) throws Exception {
			System.out.println("request feign TCC, xid:" + RootContext.getXID());

			ConsumerSystemPO po = new ConsumerSystemPO();
			po.setId(id);
			po.setName(name);
			po.setAppId(System.currentTimeMillis() + "");
			po.setEmail("e");
			po.setSaslPassword("aaa");
			po.setSaslUsername("aaa");

			po.setCreatedBy("xff");
			po.setCreatedAt(SystemUtils.now());
			po.setUpdatedBy("xff");
			po.setUpdatedAt(po.getUpdatedAt());
			consumerSystemMapper.add(po);

			return po;
		}

		public void tcc2commit(BusinessActionContext context) {
			System.out.println("tcc2commit");
			
			Map<String, Object> actionContext = context.getActionContext();
			System.out.println(actionContext);
		}

		public void tcc2rollback(BusinessActionContext context) {
			System.out.println("tcc2rollback");
			
			Map<String, Object> actionContext = context.getActionContext();
			System.out.println(actionContext);
			
			Object id = actionContext.get("id");
			consumerSystemMapper.delete((Long)id);
		}
	}

}
