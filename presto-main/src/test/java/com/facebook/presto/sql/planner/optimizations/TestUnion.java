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
package com.facebook.presto.sql.planner.optimizations;

import com.facebook.presto.sql.planner.LogicalPlanner;
import com.facebook.presto.sql.planner.Plan;
import com.facebook.presto.sql.planner.assertions.BasePlanTest;
import com.facebook.presto.sql.planner.plan.AggregationNode;
import com.facebook.presto.sql.planner.plan.ExchangeNode;
import com.facebook.presto.sql.planner.plan.JoinNode;
import com.facebook.presto.sql.planner.plan.PlanNode;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static com.facebook.presto.sql.planner.optimizations.PlanNodeSearcher.searchFrom;
import static com.facebook.presto.sql.planner.plan.ExchangeNode.Scope.REMOTE;
import static com.facebook.presto.sql.planner.plan.ExchangeNode.Type.GATHER;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class TestUnion
        extends BasePlanTest
{
    public TestUnion()
    {
        super();
    }

    public TestUnion(Map<String, String> sessionProperties)
    {
        super(sessionProperties);
    }

    @Test
    public void testPartialAggregationsWithUnion()
    {
        Plan plan = plan(
                "SELECT orderstatus, sum(orderkey) FROM (SELECT orderkey, orderstatus FROM orders UNION ALL SELECT orderkey, orderstatus FROM orders) x GROUP BY (orderstatus)",
                LogicalPlanner.Stage.OPTIMIZED_AND_VALIDATED,
                false);
        assertAtMostOneAggregationBetweenRemoteExchanges(plan);
        assertPlanIsFullyDistributed(plan);
    }

    @Test
    public void testPartialRollupAggregationsWithUnion()
    {
        Plan plan = plan(
                "SELECT orderstatus, sum(orderkey) FROM (SELECT orderkey, orderstatus FROM orders UNION ALL SELECT orderkey, orderstatus FROM orders) x GROUP BY ROLLUP (orderstatus)",
                LogicalPlanner.Stage.OPTIMIZED_AND_VALIDATED,
                false);
        assertAtMostOneAggregationBetweenRemoteExchanges(plan);
        assertPlanIsFullyDistributed(plan);
    }

    @Test
    public void testAggregationWithUnionAndValues()
    {
        Plan plan = plan(
                "SELECT regionkey, count(*) FROM (SELECT regionkey FROM nation UNION ALL SELECT * FROM (VALUES 2, 100) t(regionkey)) GROUP BY regionkey",
                LogicalPlanner.Stage.OPTIMIZED_AND_VALIDATED,
                false);
        assertAtMostOneAggregationBetweenRemoteExchanges(plan);
        // TODO: Enable this check once distributed UNION can handle both partitioned and single node sources at the same time
        //assertPlanIsFullyDistributed(plan);
    }

    @Test
    public void testUnionOnProbeSide()
    {
        Plan plan = plan(
                "SELECT * FROM (SELECT * FROM nation UNION ALL SELECT * from nation) n, region r WHERE n.regionkey=r.regionkey",
                LogicalPlanner.Stage.OPTIMIZED_AND_VALIDATED,
                false);

        assertPlanIsFullyDistributed(plan);
    }

    private void assertPlanIsFullyDistributed(Plan plan)
    {
        assertTrue(
                searchFrom(plan.getRoot())
                        .skipOnlyWhen(TestUnion::isNotRemoteGatheringExchange)
                        .findAll()
                        .stream()
                        .noneMatch(planNode -> planNode instanceof AggregationNode || planNode instanceof JoinNode),
                "There is an Aggregation or Join between output and first REMOTE GATHER ExchangeNode");

        List<PlanNode> gathers = searchFrom(plan.getRoot())
                .where(TestUnion::isRemoteGatheringExchange)
                .findAll()
                .stream()
                .collect(toList());

        assertEquals(gathers.size(), 1, "Only a single REMOTE GATHER was expected");
    }

    private static void assertAtMostOneAggregationBetweenRemoteExchanges(Plan plan)
    {
        List<PlanNode> fragments = searchFrom(plan.getRoot())
                .where(TestUnion::isRemoteExchange)
                .findAll()
                .stream()
                .flatMap(exchangeNode -> exchangeNode.getSources().stream())
                .collect(toList());

        for (PlanNode fragment : fragments) {
            List<PlanNode> aggregations = searchFrom(fragment)
                    .where(AggregationNode.class::isInstance)
                    .skipOnlyWhen(TestUnion::isNotRemoteExchange)
                    .findAll();

            assertFalse(aggregations.size() > 1, "More than a single AggregationNode between remote exchanges");
        }
    }

    private static boolean isNotRemoteGatheringExchange(PlanNode planNode)
    {
        return !isRemoteGatheringExchange(planNode);
    }

    private static boolean isRemoteGatheringExchange(PlanNode planNode)
    {
        return isRemoteExchange(planNode) && ((ExchangeNode) planNode).getType().equals(GATHER);
    }

    private static boolean isNotRemoteExchange(PlanNode planNode)
    {
        return !isRemoteExchange(planNode);
    }

    private static boolean isRemoteExchange(PlanNode planNode)
    {
        return (planNode instanceof ExchangeNode) && ((ExchangeNode) planNode).getScope().equals(REMOTE);
    }
}
