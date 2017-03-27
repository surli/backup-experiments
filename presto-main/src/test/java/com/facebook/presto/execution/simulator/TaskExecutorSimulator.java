/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.execution.simulator;

import com.facebook.presto.execution.TaskExecutor;
import com.facebook.presto.execution.simulator.SimulationController.TaskSpecification;
import com.facebook.presto.execution.simulator.SplitGenerators.AggregatedLeafSplitGenerator;
import com.facebook.presto.execution.simulator.SplitGenerators.FastLeafSplitGenerator;
import com.facebook.presto.execution.simulator.SplitGenerators.IntermediateSplitGenerator;
import com.facebook.presto.execution.simulator.SplitGenerators.L4LeafSplitGenerator;
import com.facebook.presto.execution.simulator.SplitGenerators.QuantaExceedingSplitGenerator;
import com.facebook.presto.execution.simulator.SplitGenerators.SlowLeafSplitGenerator;
import com.google.common.base.Ticker;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;
import com.google.common.util.concurrent.ListeningExecutorService;
import io.airlift.units.Duration;
import org.joda.time.DateTime;

import java.io.Closeable;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import static com.facebook.presto.execution.simulator.Histogram.fromContinuous;
import static com.facebook.presto.execution.simulator.Histogram.fromDiscrete;
import static com.facebook.presto.execution.simulator.SimulationController.TaskSpecification.Type.INTERMEDIATE;
import static com.facebook.presto.execution.simulator.SimulationController.TaskSpecification.Type.LEAF;
import static com.google.common.util.concurrent.MoreExecutors.listeningDecorator;
import static io.airlift.concurrent.Threads.threadsNamed;
import static io.airlift.units.Duration.nanosSince;
import static io.airlift.units.Duration.succinctNanos;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.concurrent.Executors.newScheduledThreadPool;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.function.Function.identity;

public class TaskExecutorSimulator
        implements Closeable
{
    public static void main(String[] args)
            throws Exception
    {
        try (TaskExecutorSimulator simulator = new TaskExecutorSimulator()) {
            simulator.run();
        }
    }

    private final ListeningExecutorService submissionExecutor = listeningDecorator(newCachedThreadPool(threadsNamed(getClass().getSimpleName() + "-%s")));
    private final ScheduledExecutorService overallStatusPrintExecutor = newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService runningSplitsPrintExecutor = newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService wakeupExecutor = newScheduledThreadPool(32);

    private final TaskExecutor taskExecutor;

    private TaskExecutorSimulator()
    {
        taskExecutor = new TaskExecutor(36, 72, Ticker.systemTicker());
        taskExecutor.start();
    }

    @Override
    public void close()
    {
        submissionExecutor.shutdownNow();
        overallStatusPrintExecutor.shutdownNow();
        runningSplitsPrintExecutor.shutdownNow();
        wakeupExecutor.shutdownNow();
        taskExecutor.stop();
    }

    public void run()
            throws Exception
    {
        long start = System.nanoTime();
        scheduleStatusPrinter(start);

        SimulationController controller = new SimulationController(taskExecutor, TaskExecutorSimulator::printSummaryStats);

        // Uncomment one of these:
        // runExperimentOverloadedCluster();
//        runExperimentMisbehavingQuanta(controller);
         runExperimentStarveSlowSplits(controller);

        System.out.println("Stopped scheduling new tasks. Ending simulation..");
        controller.stop();
        close();

        SECONDS.sleep(5);

        System.out.println();
        System.out.println("Simulation finished at " + DateTime.now() + ". Runtime: " + nanosSince(start));
        System.out.println();

        printSummaryStats(controller, taskExecutor);
    }

    private void runExperimentOverloadedCluster(SimulationController controller)
            throws InterruptedException
    {
        /*
        Designed to simulate a somewhat overloaded Hive cluster.
        The following data is a point-in-time snapshot representative production cluster:
            - 60 running queries => 45 queries/node
            - 80 tasks/node
            - 600 splits scheduled/node (80% intermediate => ~480, 20% leaf => 120)
            - Only 60% intermediate splits will ever get data (~300)
         */

        TaskSpecification leafSpec = new TaskSpecification(LEAF, "leaf", 16, 30, 0, new AggregatedLeafSplitGenerator());
        controller.addTaskSpecification(leafSpec);

        TaskSpecification slowLeafSpec = new TaskSpecification(LEAF, "slow_leaf", 16, 10, 0, new SlowLeafSplitGenerator());
        controller.addTaskSpecification(slowLeafSpec);

        TaskSpecification intermediateSpec = new TaskSpecification(INTERMEDIATE, "intermediate", 8, 40, 0, new IntermediateSplitGenerator(wakeupExecutor));
        controller.addTaskSpecification(intermediateSpec);

        controller.enableSpec(leafSpec);
        controller.enableSpec(slowLeafSpec);
        controller.enableSpec(intermediateSpec);
        controller.run();

        SECONDS.sleep(30);

        // this gets the executor into a more realistic point-in-time state, where long running tasks start to make progress
        for (int i = 0; i < 10; i++) {
            controller.clearPendingQueue();
            MINUTES.sleep(1);
        }

        System.out.println("Overload experiment completed.");
    }

    private void runExperimentStarveSlowSplits(SimulationController controller)
            throws InterruptedException
    {
        System.out.println("Starvation experiment started.");
        TaskSpecification slowLeafSpec = new TaskSpecification(LEAF, "slow_leaf", 600, 40, 4, new SlowLeafSplitGenerator());
        controller.addTaskSpecification(slowLeafSpec);

        TaskSpecification intermediateSpec = new TaskSpecification(INTERMEDIATE, "intermediate", 400, 40, 8, new IntermediateSplitGenerator(wakeupExecutor));
        controller.addTaskSpecification(intermediateSpec);

        TaskSpecification fastLeafSpec = new TaskSpecification(LEAF, "fast_leaf", 600, 40, 4, new FastLeafSplitGenerator());
        controller.addTaskSpecification(fastLeafSpec);

        controller.enableSpec(slowLeafSpec);
        controller.enableSpec(fastLeafSpec);
        controller.enableSpec(intermediateSpec);

        controller.run();

        for (int i = 0; i < 120; i++) {
            SECONDS.sleep(10);
            controller.clearPendingQueue();
        }

        System.out.println("Starvation experiment completed.");
    }

    private void runExperimentMisbehavingQuanta(SimulationController controller)
            throws InterruptedException
    {
        System.out.println("Misbehaving quanta experiment started.");

        TaskSpecification slowLeafSpec = new TaskSpecification(LEAF, "l4_leaf", 0, 16, 4, new L4LeafSplitGenerator());
        controller.addTaskSpecification(slowLeafSpec);

        TaskSpecification misbehavingLeafSpec = new TaskSpecification(LEAF, "misbehaving_leaf", 0, 16, 4, new QuantaExceedingSplitGenerator());
        controller.addTaskSpecification(misbehavingLeafSpec);

        controller.enableSpec(slowLeafSpec);
        controller.enableSpec(misbehavingLeafSpec);

        controller.run();

        for (int i = 0; i < 120; i++) {
            controller.clearPendingQueue();
            SECONDS.sleep(20);
        }

        System.out.println("Misbehaving quanta experiment completed.");
    }

    private void scheduleStatusPrinter(long start)
    {
        overallStatusPrintExecutor.scheduleAtFixedRate(() -> {
            try {
                System.out.printf(
                        "%6s -- Running: %4s  All: %4s  L: %4s  I: %4s  Blocked: %4s  Pending: %4s  Completed: %4s  Running Tasks Total: %3s L: %3s %3s %3s %3s %3s\n",
                        nanosSince(start),
                        taskExecutor.getRunningSplits(),
                        taskExecutor.getTotalSplits(),
                        taskExecutor.getTotalSplits() - taskExecutor.getForcedRunningSplits(),
                        taskExecutor.getForcedRunningSplits(),
                        taskExecutor.getBlockedSplits(),
                        taskExecutor.getPendingSplits(),
                        taskExecutor.getCompletedSplitsLevel0() + taskExecutor.getCompletedSplitsLevel1() + taskExecutor.getCompletedSplitsLevel2() + taskExecutor.getCompletedSplitsLevel3() + taskExecutor.getCompletedSplitsLevel4(),
                        taskExecutor.getTasks(),
                        taskExecutor.getRunningTasksLevel0(),
                        taskExecutor.getRunningTasksLevel1(),
                        taskExecutor.getRunningTasksLevel2(),
                        taskExecutor.getRunningTasksLevel3(),
                        taskExecutor.getRunningTasksLevel4());
            }
            catch (Exception ignored) {
            }
        }, 1, 1, SECONDS);
    }

    private static void printSummaryStats(SimulationController controller, TaskExecutor taskExecutor)
    {
        Map<TaskSpecification, Boolean> specEnabled = controller.getSpecEnabled();

        ListMultimap<TaskSpecification, SimulationTask> completedTasks = controller.getCompletedTasks();
        ListMultimap<TaskSpecification, SimulationTask> runningTasks = controller.getRunningTasks();
        Set<SimulationTask> allTasks = ImmutableSet.<SimulationTask>builder().addAll(completedTasks.values()).addAll(runningTasks.values()).build();

        long completedSplits = completedTasks.values().stream().mapToInt(t -> t.getCompletedSplits().size()).sum();
        long runningSplits = runningTasks.values().stream().mapToInt(t -> t.getCompletedSplits().size()).sum();

        System.out.println("Completed tasks : " + completedTasks.size());
        System.out.println("Remaining tasks : " + runningTasks.size());
        System.out.println("Completed splits: " + completedSplits);
        System.out.println("Remaining splits: " + runningSplits);
        System.out.println();
        System.out.println("Completed tasks  L0: " + taskExecutor.getCompletedTasksLevel0());
        System.out.println("Completed tasks  L1: " + taskExecutor.getCompletedTasksLevel1());
        System.out.println("Completed tasks  L2: " + taskExecutor.getCompletedTasksLevel2());
        System.out.println("Completed tasks  L3: " + taskExecutor.getCompletedTasksLevel3());
        System.out.println("Completed tasks  L4: " + taskExecutor.getCompletedTasksLevel4());
        System.out.println();
        System.out.println("Completed splits L0: " + taskExecutor.getCompletedSplitsLevel0());
        System.out.println("Completed splits L1: " + taskExecutor.getCompletedSplitsLevel1());
        System.out.println("Completed splits L2: " + taskExecutor.getCompletedSplitsLevel2());
        System.out.println("Completed splits L3: " + taskExecutor.getCompletedSplitsLevel3());
        System.out.println("Completed splits L4: " + taskExecutor.getCompletedSplitsLevel4());

        Histogram<Long> levelsHistogram = fromContinuous(ImmutableList.of(
                MILLISECONDS.toNanos(0L),
                MILLISECONDS.toNanos(1_000),
                MILLISECONDS.toNanos(10_000L),
                MILLISECONDS.toNanos(60_000L),
                MILLISECONDS.toNanos(300_000L),
                HOURS.toNanos(1),
                DAYS.toNanos(1)));

        System.out.println();
        System.out.println("Levels - Completed Leaf Task Processed Time");
        levelsHistogram.printDistribution(
                completedTasks.values().stream().filter(t -> t.getSpecification().getType() == LEAF).collect(Collectors.toList()),
                SimulationTask::getScheduledTimeNanos,
                SimulationTask::getProcessedTimeNanos,
                Duration::succinctNanos,
                TaskExecutorSimulator::formatNanos);

        System.out.println();
        System.out.println("Levels - Running Leaf Task Processed Time");
        levelsHistogram.printDistribution(
                runningTasks.values().stream().filter(t -> t.getSpecification().getType() == LEAF).collect(Collectors.toList()),
                SimulationTask::getScheduledTimeNanos,
                SimulationTask::getProcessedTimeNanos,
                Duration::succinctNanos,
                TaskExecutorSimulator::formatNanos);

        System.out.println();
        System.out.println("Levels - Completed Intermediate Task Wall Time");
        levelsHistogram.printDistribution(
                completedTasks.values().stream().filter(t -> t.getSpecification().getType() == INTERMEDIATE).collect(Collectors.toList()),
                SimulationTask::getScheduledTimeNanos,
                SimulationTask::getProcessedTimeNanos,
                Duration::succinctNanos,
                TaskExecutorSimulator::formatNanos);

        System.out.println();
        System.out.println("Levels - Running Intermediate Task Wall Time");
        levelsHistogram.printDistribution(
                runningTasks.values().stream().filter(t -> t.getSpecification().getType() == INTERMEDIATE).collect(Collectors.toList()),
                SimulationTask::getScheduledTimeNanos,
                SimulationTask::getProcessedTimeNanos,
                Duration::succinctNanos,
                TaskExecutorSimulator::formatNanos);

        System.out.println();
        System.out.println("Specification - Wait times");
        List<String> specifications = runningTasks.values().stream().map(t -> t.getSpecification().getName()).collect(Collectors.toList());
        fromDiscrete(specifications).printDistribution(
                allTasks,
                t -> t.getSpecification().getName(),
                SimulationTask::getTotalWaitTimeNanos,
                identity(),
                TaskExecutorSimulator::formatNanos);

        System.out.println();
        System.out.println("Breakdown by specification");
        System.out.println("##########################");
        for (TaskSpecification specification : specEnabled.keySet()) {
            List<SimulationTask> allSpecificationTasks = ImmutableList.<SimulationTask>builder()
                    .addAll(completedTasks.get(specification))
                    .addAll(runningTasks.get(specification))
                    .build();

            System.out.println(specification.getName());
            System.out.println("=============================");
            System.out.println("Completed tasks           : " + completedTasks.get(specification).size());
            System.out.println("In-progress tasks         : " + runningTasks.get(specification).size());
            System.out.println("Total tasks               : " + specification.getTotalTasks());
            System.out.println("Splits/task               : " + specification.getNumSplitsPerTask());
            System.out.println("Current required time     : " + succinctNanos(allSpecificationTasks.stream().mapToLong(SimulationTask::getScheduledTimeNanos).sum()));
            System.out.println("Completed scheduled time  : " + succinctNanos(allSpecificationTasks.stream().mapToLong(SimulationTask::getProcessedTimeNanos).sum()));
            System.out.println("Total wait time           : " + succinctNanos(allSpecificationTasks.stream().mapToLong(SimulationTask::getTotalWaitTimeNanos).sum()));

            System.out.println("All Tasks by scheduled time - Wait Time");
            levelsHistogram.printDistribution(
                    allSpecificationTasks,
                    SimulationTask::getScheduledTimeNanos,
                    SimulationTask::getTotalWaitTimeNanos,
                    Duration::succinctNanos,
                    TaskExecutorSimulator::formatNanos);

            System.out.println("Complete Tasks by scheduled time - Wait Time");
            levelsHistogram.printDistribution(
                    completedTasks.get(specification),
                    SimulationTask::getScheduledTimeNanos,
                    SimulationTask::getTotalWaitTimeNanos,
                    Duration::succinctNanos,
                    TaskExecutorSimulator::formatNanos);
            System.out.println();
        }
    }

    private static String formatNanos(List<Long> list)
    {
        LongSummaryStatistics stats = list.stream().mapToLong(Long::new).summaryStatistics();
        return String.format("Min: %8s  Max: %8s  Avg: %8s  Sum: %8s",
                succinctNanos(stats.getMin() == Long.MAX_VALUE ? 0 : stats.getMin()),
                succinctNanos(stats.getMax() == Long.MIN_VALUE ? 0 : stats.getMax()),
                succinctNanos((long) stats.getAverage()),
                succinctNanos(stats.getSum()));
    }
}
