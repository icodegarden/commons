/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package basic.demo.paramflow;

import java.util.Collections;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowItem;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;

/**
 * This demo demonstrates flow control by frequent ("hot spot") parameters.
 *
 * @author Eric Zhao
 * @since 0.2.0
 */
/**
[1][1666148714529] Parameter flow metrics for resource resA: pass count for param <1> is 5, block count: 997
[1][1666148714529] Parameter flow metrics for resource resA: pass count for param <2> is 10, block count: 915
[1][1666148714529] Parameter flow metrics for resource resA: pass count for param <3> is 5, block count: 951
[1][1666148714529] Parameter flow metrics for resource resA: pass count for param <4> is 5, block count: 947
=============================
[0][1666148715530] Parameter flow metrics for resource resA: pass count for param <1> is 5, block count: 966
[0][1666148715530] Parameter flow metrics for resource resA: pass count for param <2> is 10, block count: 945
[0][1666148715530] Parameter flow metrics for resource resA: pass count for param <3> is 5, block count: 941
[0][1666148715530] Parameter flow metrics for resource resA: pass count for param <4> is 5, block count: 992
=============================
...

针对热点参数（数据）的限流，本例有4个参数（1，2，3，4），规则是对索引位是0的参数限制qps=5，但单独设置参数=2时qps=10，因此不同的参数都是qps5，但PARAM_B=2时qps10
 * @author Fangfang.Xu
 *
 */
public class ParamFlowQpsDemo {

    private static final int PARAM_A = 1;
    private static final int PARAM_B = 2;
    private static final int PARAM_C = 3;
    private static final int PARAM_D = 4;

    /**
     * Here we prepare different parameters to validate flow control by parameters.
     */
    private static final Integer[] PARAMS = new Integer[] {PARAM_A, PARAM_B, PARAM_C, PARAM_D};

    private static final String RESOURCE_KEY = "resA";

    public static void main(String[] args) throws Exception {
        initParamFlowRules();

        final int threadCount = 20;
        ParamFlowQpsRunner<Integer> runner = new ParamFlowQpsRunner<>(PARAMS, RESOURCE_KEY, threadCount, 120);
        runner.tick();

        Thread.sleep(1000);
        runner.simulateTraffic();
    }

    private static void initParamFlowRules() {
        // QPS mode, threshold is 5 for every frequent "hot spot" parameter in index 0 (the first arg).
        ParamFlowRule rule = new ParamFlowRule(RESOURCE_KEY)
            .setParamIdx(0)
            .setGrade(RuleConstant.FLOW_GRADE_QPS)
            //.setDurationInSec(3)
            //.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER)
            //.setMaxQueueingTimeMs(600)
            .setCount(5);

        // We can set threshold count for specific parameter value individually.
        // Here we add an exception item. That means: QPS threshold of entries with parameter `PARAM_B` (type: int)
        // in index 0 will be 10, rather than the global threshold (5).
        ParamFlowItem item = new ParamFlowItem().setObject(String.valueOf(PARAM_B))
            .setClassType(int.class.getName())
            .setCount(10);
        rule.setParamFlowItemList(Collections.singletonList(item));
        ParamFlowRuleManager.loadRules(Collections.singletonList(rule));
    }
}
