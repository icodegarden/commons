package sentinel.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreakerStrategy;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class SentinetlTests {

	public static void main(String[] args) throws Exception {
		// 配置规则.
		initFlowRules();

		while (true) {
			// 必须的
			ContextUtil.enter("HelloWorld", "appA");
			// 1.5.0 版本开始可以直接利用 try-with-resources 特性
			Entry entry = null;
			try {
				entry = SphU.entry("HelloWorld");
				// 被保护的逻辑
				Thread.sleep(1);
//				Thread.sleep(1/0);
				System.out.println("pass");
			} catch (SystemBlockException e) {
				System.out.println("1----" + e.getResourceName());
			} catch (AuthorityException e) {
				System.out.println("2----" + e.getRule().getResource());
			} catch (FlowException e) {
				System.out.println("3----" + e.getRule().getResource());
			} catch (DegradeException e) {
				// 熔断降级 参考aop demo
				System.out.println("4----" + e.getRule().getResource());
			} catch (Exception e) {
				System.out.println(e);// biz ex
				Tracer.traceEntry(e, entry);
				Thread.sleep(1000);
			} finally {
				if(entry != null) {
					entry.exit();	
				}
				ContextUtil.exit();
			}
		}
	}

	private static void initFlowRules() {
		List<FlowRule> rules = new ArrayList<>();
		FlowRule rule = new FlowRule();
		rule.setResource("HelloWorld");
		rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
		// Set limit QPS to 20.
		rule.setCount(20);
		rules.add(rule);
		FlowRuleManager.loadRules(rules);

//		AuthorityRule arule = new AuthorityRule();
//        arule.setResource("HelloWorld");
//        arule.setStrategy(RuleConstant.AUTHORITY_BLACK);
//        arule.setLimitApp("appA,appB");
//        AuthorityRuleManager.loadRules(Collections.singletonList(arule));

		List<DegradeRule> drules = new ArrayList<>();
		DegradeRule drule = new DegradeRule("HelloWorld")
				.setGrade(CircuitBreakerStrategy.ERROR_RATIO.getType())
				// Set ratio threshold to 50%.
				.setCount(0.5d)
				.setStatIntervalMs(1000)
				.setMinRequestAmount(1)
				// Retry timeout (in second)
				.setTimeWindow(3);
		
		drules.add(drule);
		DegradeRuleManager.loadRules(drules);
	}
}
