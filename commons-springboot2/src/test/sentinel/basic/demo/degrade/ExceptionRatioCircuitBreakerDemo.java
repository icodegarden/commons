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
package basic.demo.degrade;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreaker.State;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreakerStrategy;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.EventObserverRegistry;
import com.alibaba.csp.sentinel.util.TimeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author jialiang.linjl
 * @author Eric Zhao
 */
/**
1665727143377, oneSecondTotal:734, oneSecondPass:734, oneSecondBlock:0, oneSecondBizException:348
1665727144378, oneSecondTotal:1038, oneSecondPass:1038, oneSecondBlock:0, oneSecondBizException:457
1665727145379, oneSecondTotal:1044, oneSecondPass:1045, oneSecondBlock:0, oneSecondBizException:448
1665727146378, oneSecondTotal:1032, oneSecondPass:1031, oneSecondBlock:0, oneSecondBizException:457
1665727147379, oneSecondTotal:1038, oneSecondPass:1038, oneSecondBlock:0, oneSecondBizException:467
1665727148380, oneSecondTotal:1035, oneSecondPass:1035, oneSecondBlock:0, oneSecondBizException:474
1665727149379, oneSecondTotal:1032, oneSecondPass:1032, oneSecondBlock:0, oneSecondBizException:452
1665727150379, oneSecondTotal:1059, oneSecondPass:1060, oneSecondBlock:0, oneSecondBizException:483
1665727151382, oneSecondTotal:1044, oneSecondPass:1046, oneSecondBlock:0, oneSecondBizException:471
1665727152381, oneSecondTotal:1056, oneSecondPass:1053, oneSecondBlock:0, oneSecondBizException:466
1665727153382, oneSecondTotal:1042, oneSecondPass:1044, oneSecondBlock:0, oneSecondBizException:443
1665727154384, oneSecondTotal:1041, oneSecondPass:1039, oneSecondBlock:0, oneSecondBizException:473
1665727155383, oneSecondTotal:1035, oneSecondPass:1036, oneSecondBlock:0, oneSecondBizException:440
1665727156384, oneSecondTotal:1047, oneSecondPass:1046, oneSecondBlock:0, oneSecondBizException:453
1665727157384, oneSecondTotal:1036, oneSecondPass:1036, oneSecondBlock:0, oneSecondBizException:471
1665727158385, oneSecondTotal:1040, oneSecondPass:1040, oneSecondBlock:0, oneSecondBizException:461
1665727159384, oneSecondTotal:1043, oneSecondPass:1043, oneSecondBlock:0, oneSecondBizException:469
1665727160385, oneSecondTotal:1045, oneSecondPass:1045, oneSecondBlock:0, oneSecondBizException:481
1665727161384, oneSecondTotal:1045, oneSecondPass:1045, oneSecondBlock:0, oneSecondBizException:443
1665727162386, oneSecondTotal:1029, oneSecondPass:1029, oneSecondBlock:0, oneSecondBizException:436
1665727163386, oneSecondTotal:1049, oneSecondPass:1049, oneSecondBlock:0, oneSecondBizException:452
1665727164386, oneSecondTotal:1043, oneSecondPass:1044, oneSecondBlock:0, oneSecondBizException:442
1665727165386, oneSecondTotal:1027, oneSecondPass:1026, oneSecondBlock:0, oneSecondBizException:456
1665727166388, oneSecondTotal:1037, oneSecondPass:1037, oneSecondBlock:0, oneSecondBizException:465
1665727167388, oneSecondTotal:1035, oneSecondPass:1035, oneSecondBlock:0, oneSecondBizException:484
1665727168388, oneSecondTotal:1031, oneSecondPass:1032, oneSecondBlock:0, oneSecondBizException:435
1665727169389, oneSecondTotal:1046, oneSecondPass:1046, oneSecondBlock:0, oneSecondBizException:457
CLOSED -> OPEN at 1665727170049, snapshotValue=0.54
1665727170388, oneSecondTotal:1025, oneSecondPass:692, oneSecondBlock:340, oneSecondBizException:298
1665727171389, oneSecondTotal:1043, oneSecondPass:0, oneSecondBlock:1042, oneSecondBizException:0
1665727172389, oneSecondTotal:1045, oneSecondPass:0, oneSecondBlock:1046, oneSecondBizException:0
1665727173390, oneSecondTotal:1044, oneSecondPass:0, oneSecondBlock:1042, oneSecondBizException:0
1665727174391, oneSecondTotal:1039, oneSecondPass:0, oneSecondBlock:1041, oneSecondBizException:0
1665727175392, oneSecondTotal:1028, oneSecondPass:0, oneSecondBlock:1028, oneSecondBizException:0
1665727176392, oneSecondTotal:1036, oneSecondPass:0, oneSecondBlock:1036, oneSecondBizException:0
1665727177392, oneSecondTotal:1044, oneSecondPass:0, oneSecondBlock:1042, oneSecondBizException:0
1665727178394, oneSecondTotal:1041, oneSecondPass:0, oneSecondBlock:1042, oneSecondBizException:0
1665727179394, oneSecondTotal:1052, oneSecondPass:0, oneSecondBlock:1052, oneSecondBizException:0
OPEN -> HALF_OPEN at 1665727180052
HALF_OPEN -> CLOSED at 1665727180060
CLOSED -> OPEN at 1665727180119, snapshotValue=0.51
1665727180392, oneSecondTotal:1046, oneSecondPass:61, oneSecondBlock:985, oneSecondBizException:31
1665727181393, oneSecondTotal:1053, oneSecondPass:0, oneSecondBlock:1054, oneSecondBizException:0
1665727182394, oneSecondTotal:1048, oneSecondPass:0, oneSecondBlock:1046, oneSecondBizException:0
1665727183395, oneSecondTotal:1034, oneSecondPass:0, oneSecondBlock:1035, oneSecondBizException:0
1665727184394, oneSecondTotal:1041, oneSecondPass:0, oneSecondBlock:1041, oneSecondBizException:0
1665727185396, oneSecondTotal:1036, oneSecondPass:0, oneSecondBlock:1037, oneSecondBizException:0
1665727186395, oneSecondTotal:1024, oneSecondPass:0, oneSecondBlock:1024, oneSecondBizException:0
1665727187396, oneSecondTotal:1044, oneSecondPass:0, oneSecondBlock:1043, oneSecondBizException:0
1665727188397, oneSecondTotal:1040, oneSecondPass:0, oneSecondBlock:1041, oneSecondBizException:0
1665727189398, oneSecondTotal:1040, oneSecondPass:0, oneSecondBlock:1040, oneSecondBizException:0
OPEN -> HALF_OPEN at 1665727190119
HALF_OPEN -> OPEN at 1665727190127, snapshotValue=1.00
1665727190397, oneSecondTotal:1050, oneSecondPass:1, oneSecondBlock:1049, oneSecondBizException:1
1665727191399, oneSecondTotal:1055, oneSecondPass:0, oneSecondBlock:1054, oneSecondBizException:0
1665727192399, oneSecondTotal:1037, oneSecondPass:0, oneSecondBlock:1037, oneSecondBizException:0
1665727193398, oneSecondTotal:1041, oneSecondPass:0, oneSecondBlock:1042, oneSecondBizException:0
1665727194400, oneSecondTotal:1043, oneSecondPass:0, oneSecondBlock:1042, oneSecondBizException:0
1665727195401, oneSecondTotal:1055, oneSecondPass:0, oneSecondBlock:1054, oneSecondBizException:0
1665727196401, oneSecondTotal:1050, oneSecondPass:0, oneSecondBlock:1052, oneSecondBizException:0
1665727197403, oneSecondTotal:1037, oneSecondPass:0, oneSecondBlock:1037, oneSecondBizException:0
1665727198402, oneSecondTotal:1041, oneSecondPass:0, oneSecondBlock:1041, oneSecondBizException:0
1665727199403, oneSecondTotal:1039, oneSecondPass:0, oneSecondBlock:1039, oneSecondBizException:0
OPEN -> HALF_OPEN at 1665727200127
HALF_OPEN -> CLOSED at 1665727200135
1665727200402, oneSecondTotal:1053, oneSecondPass:277, oneSecondBlock:768, oneSecondBizException:121
1665727201404, oneSecondTotal:1048, oneSecondPass:1048, oneSecondBlock:0, oneSecondBizException:452
1665727202402, oneSecondTotal:1027, oneSecondPass:1027, oneSecondBlock:0, oneSecondBizException:412
1665727203404, oneSecondTotal:1049, oneSecondPass:1049, oneSecondBlock:0, oneSecondBizException:483
1665727204406, oneSecondTotal:1046, oneSecondPass:1047, oneSecondBlock:0, oneSecondBizException:450
1665727205404, oneSecondTotal:1044, oneSecondPass:1043, oneSecondBlock:0, oneSecondBizException:441
1665727206404, oneSecondTotal:1035, oneSecondPass:1036, oneSecondBlock:0, oneSecondBizException:447
1665727207406, oneSecondTotal:1026, oneSecondPass:1025, oneSecondBlock:0, oneSecondBizException:463
1665727208406, oneSecondTotal:1045, oneSecondPass:1045, oneSecondBlock:0, oneSecondBizException:459
1665727209405, oneSecondTotal:1035, oneSecondPass:1035, oneSecondBlock:0, oneSecondBizException:465
1665727210406, oneSecondTotal:1038, oneSecondPass:1039, oneSecondBlock:0, oneSecondBizException:470
1665727211407, oneSecondTotal:1035, oneSecondPass:1034, oneSecondBlock:0, oneSecondBizException:465
1665727212408, oneSecondTotal:1035, oneSecondPass:1035, oneSecondBlock:0, oneSecondBizException:469
1665727213407, oneSecondTotal:1039, oneSecondPass:1039, oneSecondBlock:0, oneSecondBizException:421
1665727214409, oneSecondTotal:1040, oneSecondPass:1040, oneSecondBlock:0, oneSecondBizException:446
1665727215410, oneSecondTotal:1047, oneSecondPass:1047, oneSecondBlock:0, oneSecondBizException:432
1665727216410, oneSecondTotal:1051, oneSecondPass:1051, oneSecondBlock:0, oneSecondBizException:452
1665727217411, oneSecondTotal:1034, oneSecondPass:1034, oneSecondBlock:0, oneSecondBizException:445
1665727218410, oneSecondTotal:1032, oneSecondPass:1032, oneSecondBlock:0, oneSecondBizException:433
1665727219412, oneSecondTotal:1032, oneSecondPass:1032, oneSecondBlock:0, oneSecondBizException:434
1665727220413, oneSecondTotal:1043, oneSecondPass:1043, oneSecondBlock:0, oneSecondBizException:441
1665727221412, oneSecondTotal:1032, oneSecondPass:1032, oneSecondBlock:0, oneSecondBizException:439
1665727222412, oneSecondTotal:1046, oneSecondPass:1046, oneSecondBlock:0, oneSecondBizException:481
1665727223414, oneSecondTotal:1064, oneSecondPass:1064, oneSecondBlock:0, oneSecondBizException:445
1665727224414, oneSecondTotal:1049, oneSecondPass:1050, oneSecondBlock:0, oneSecondBizException:452
1665727225414, oneSecondTotal:1043, oneSecondPass:1042, oneSecondBlock:0, oneSecondBizException:465
1665727226415, oneSecondTotal:1043, oneSecondPass:1043, oneSecondBlock:0, oneSecondBizException:430
1665727227415, oneSecondTotal:1036, oneSecondPass:1036, oneSecondBlock:0, oneSecondBizException:459
1665727228416, oneSecondTotal:1045, oneSecondPass:1045, oneSecondBlock:0, oneSecondBizException:452
1665727229416, oneSecondTotal:1039, oneSecondPass:1039, oneSecondBlock:0, oneSecondBizException:469
CLOSED -> OPEN at 1665727230048, snapshotValue=0.52
1665727230416, oneSecondTotal:1044, oneSecondPass:667, oneSecondBlock:384, oneSecondBizException:277
1665727231416, oneSecondTotal:1055, oneSecondPass:0, oneSecondBlock:1056, oneSecondBizException:0
1665727232418, oneSecondTotal:1058, oneSecondPass:0, oneSecondBlock:1057, oneSecondBizException:0
1665727233418, oneSecondTotal:1034, oneSecondPass:0, oneSecondBlock:1035, oneSecondBizException:0
1665727234419, oneSecondTotal:1048, oneSecondPass:0, oneSecondBlock:1048, oneSecondBizException:0
1665727235418, oneSecondTotal:1054, oneSecondPass:0, oneSecondBlock:1054, oneSecondBizException:0
1665727236421, oneSecondTotal:1043, oneSecondPass:0, oneSecondBlock:1043, oneSecondBizException:0
1665727237420, oneSecondTotal:1041, oneSecondPass:0, oneSecondBlock:1041, oneSecondBizException:0
1665727238420, oneSecondTotal:1048, oneSecondPass:0, oneSecondBlock:1048, oneSecondBizException:0
1665727239422, oneSecondTotal:1038, oneSecondPass:0, oneSecondBlock:1038, oneSecondBizException:0
OPEN -> HALF_OPEN at 1665727240052
HALF_OPEN -> CLOSED at 1665727240060
1665727240423, oneSecondTotal:1040, oneSecondPass:373, oneSecondBlock:659, oneSecondBizException:158
1665727241422, oneSecondTotal:1041, oneSecondPass:1041, oneSecondBlock:0, oneSecondBizException:445
1665727242423, oneSecondTotal:1056, oneSecondPass:1056, oneSecondBlock:0, oneSecondBizException:449
1665727243424, oneSecondTotal:1039, oneSecondPass:1039, oneSecondBlock:0, oneSecondBizException:449
1665727244425, oneSecondTotal:1041, oneSecondPass:1041, oneSecondBlock:0, oneSecondBizException:436
1665727245425, oneSecondTotal:1038, oneSecondPass:1038, oneSecondBlock:0, oneSecondBizException:479
1665727246425, oneSecondTotal:1029, oneSecondPass:1029, oneSecondBlock:0, oneSecondBizException:458
1665727247425, oneSecondTotal:1053, oneSecondPass:1053, oneSecondBlock:0, oneSecondBizException:465
1665727248426, oneSecondTotal:1053, oneSecondPass:1053, oneSecondBlock:0, oneSecondBizException:454
1665727249427, oneSecondTotal:1037, oneSecondPass:1037, oneSecondBlock:0, oneSecondBizException:429
1665727250428, oneSecondTotal:1051, oneSecondPass:1051, oneSecondBlock:0, oneSecondBizException:471
1665727251428, oneSecondTotal:1053, oneSecondPass:1053, oneSecondBlock:0, oneSecondBizException:468
1665727252430, oneSecondTotal:1050, oneSecondPass:1050, oneSecondBlock:0, oneSecondBizException:461
1665727253428, oneSecondTotal:1043, oneSecondPass:1043, oneSecondBlock:0, oneSecondBizException:465
1665727254430, oneSecondTotal:1048, oneSecondPass:1048, oneSecondBlock:0, oneSecondBizException:417
1665727255429, oneSecondTotal:1050, oneSecondPass:1050, oneSecondBlock:0, oneSecondBizException:473
1665727256431, oneSecondTotal:1059, oneSecondPass:1059, oneSecondBlock:0, oneSecondBizException:442
1665727257431, oneSecondTotal:1043, oneSecondPass:1043, oneSecondBlock:0, oneSecondBizException:498
1665727258431, oneSecondTotal:1042, oneSecondPass:1042, oneSecondBlock:0, oneSecondBizException:458
1665727259432, oneSecondTotal:1047, oneSecondPass:1047, oneSecondBlock:0, oneSecondBizException:478
1665727260432, oneSecondTotal:1032, oneSecondPass:1033, oneSecondBlock:0, oneSecondBizException:475
1665727261433, oneSecondTotal:1038, oneSecondPass:1037, oneSecondBlock:0, oneSecondBizException:462
1665727262432, oneSecondTotal:1042, oneSecondPass:1042, oneSecondBlock:0, oneSecondBizException:471
1665727263434, oneSecondTotal:1043, oneSecondPass:1043, oneSecondBlock:0, oneSecondBizException:438
time cost: 121059 ms
total: 125797, pass:84062, block:41735, bizException:36783

按异常比例熔断，属性包含 统计间隔30000ms（是否需要关闭->打开），重试窗口期10s（打开->半打开->关闭或打开），异常比例50%，最小请求数50（窗口期内）
 * @author Fangfang.Xu
 *
 */
public class ExceptionRatioCircuitBreakerDemo {

    private static final String KEY = "some_service";

    private static AtomicInteger total = new AtomicInteger();
    private static AtomicInteger pass = new AtomicInteger();
    private static AtomicInteger block = new AtomicInteger();
    private static AtomicInteger bizException = new AtomicInteger();

    private static volatile boolean stop = false;
    private static int seconds = 120;

    public static void main(String[] args) throws Exception {
        initDegradeRule();
        registerStateChangeObserver();
        startTick();

        final int concurrency = 8;
        for (int i = 0; i < concurrency; i++) {
            Thread entryThread = new Thread(() -> {
                while (true) {
                    Entry entry = null;
                    try {
                        entry = SphU.entry(KEY);
                        sleep(ThreadLocalRandom.current().nextInt(5, 10));
                        pass.addAndGet(1);

                        // Error probability is 45%
                        if (ThreadLocalRandom.current().nextInt(0, 100) > 55) {
                            // biz code raise an exception.
                            throw new RuntimeException("oops");
                        }
                    } catch (BlockException e) {
                        block.addAndGet(1);
                        sleep(ThreadLocalRandom.current().nextInt(5, 10));
                    } catch (Throwable t) {
                        bizException.incrementAndGet();
                        // It's required to record exception here manually.
                        Tracer.traceEntry(t, entry);
                    } finally {
                        total.addAndGet(1);
                        if (entry != null) {
                            entry.exit();
                        }
                    }
                }
            });
            entryThread.setName("sentinel-simulate-traffic-task-" + i);
            entryThread.start();
        }
    }

    private static void registerStateChangeObserver() {
        EventObserverRegistry.getInstance().addStateChangeObserver("logging",
            (prevState, newState, rule, snapshotValue) -> {
                if (newState == State.OPEN) {
                    System.out.println(String.format("%s -> OPEN at %d, snapshotValue=%.2f", prevState.name(),
                        TimeUtil.currentTimeMillis(), snapshotValue));
                } else {
                    System.out.println(String.format("%s -> %s at %d", prevState.name(), newState.name(),
                        TimeUtil.currentTimeMillis()));
                }
            });
    }

    private static void initDegradeRule() {
        List<DegradeRule> rules = new ArrayList<>();
        DegradeRule rule = new DegradeRule(KEY)
            .setGrade(CircuitBreakerStrategy.ERROR_RATIO.getType())
            // Set ratio threshold to 50%.
            .setCount(0.5d)
            .setStatIntervalMs(30000)
            .setMinRequestAmount(50)
            // Retry timeout (in second)
            .setTimeWindow(10);
        rules.add(rule);
        DegradeRuleManager.loadRules(rules);
        System.out.println("Degrade rule loaded: " + rules);
    }

    private static void sleep(int timeMs) {
        try {
            TimeUnit.MILLISECONDS.sleep(timeMs);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    private static void startTick() {
        Thread timer = new Thread(new TimerTask());
        timer.setName("sentinel-timer-tick-task");
        timer.start();
    }

    static class TimerTask implements Runnable {
        @Override
        public void run() {
            long start = System.currentTimeMillis();
            System.out.println("Begin to run! Go go go!");
            System.out.println("See corresponding metrics.log for accurate statistic data");

            long oldTotal = 0;
            long oldPass = 0;
            long oldBlock = 0;
            long oldBizException = 0;
            while (!stop) {
                sleep(1000);

                long globalTotal = total.get();
                long oneSecondTotal = globalTotal - oldTotal;
                oldTotal = globalTotal;

                long globalPass = pass.get();
                long oneSecondPass = globalPass - oldPass;
                oldPass = globalPass;

                long globalBlock = block.get();
                long oneSecondBlock = globalBlock - oldBlock;
                oldBlock = globalBlock;

                long globalBizException = bizException.get();
                long oneSecondBizException = globalBizException - oldBizException;
                oldBizException = globalBizException;

                System.out.println(TimeUtil.currentTimeMillis() + ", oneSecondTotal:" + oneSecondTotal
                    + ", oneSecondPass:" + oneSecondPass
                    + ", oneSecondBlock:" + oneSecondBlock
                    + ", oneSecondBizException:" + oneSecondBizException);
                if (seconds-- <= 0) {
                    stop = true;
                }
            }
            long cost = System.currentTimeMillis() - start;
            System.out.println("time cost: " + cost + " ms");
            System.out.println("total: " + total.get() + ", pass:" + pass.get()
                + ", block:" + block.get() + ", bizException:" + bizException.get());
            System.exit(0);
        }
    }
}
