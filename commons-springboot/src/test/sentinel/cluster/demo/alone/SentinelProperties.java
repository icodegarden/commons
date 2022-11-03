package cluster.demo.alone;

import java.util.List;

import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.system.SystemRule;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Getter
@Setter
@ToString
public class SentinelProperties {
	private List<SystemRule> systems;
	private List<AuthorityRule> authoritys;
	private List<FlowRule> flows;
	private List<ParamFlowRule> paramFlows;
	private List<DegradeRule> degrades;
}