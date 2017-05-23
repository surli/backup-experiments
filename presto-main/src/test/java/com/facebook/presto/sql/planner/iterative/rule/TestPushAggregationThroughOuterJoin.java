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

package com.facebook.presto.sql.planner.iterative.rule;

import com.facebook.presto.metadata.FunctionKind;
import com.facebook.presto.metadata.Signature;
import com.facebook.presto.sql.planner.Symbol;
import com.facebook.presto.sql.planner.iterative.rule.test.RuleTester;
import com.facebook.presto.sql.planner.plan.AggregationNode;
import com.facebook.presto.sql.planner.plan.JoinNode;
import com.facebook.presto.sql.tree.FunctionCall;
import com.facebook.presto.sql.tree.QualifiedName;
import com.facebook.presto.sql.tree.SymbolReference;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.testng.annotations.Test;

import java.util.Optional;

import static com.facebook.presto.spi.type.BigintType.BIGINT;
import static com.facebook.presto.spi.type.DoubleType.DOUBLE;
import static com.facebook.presto.sql.planner.assertions.PlanMatchPattern.aggregation;
import static com.facebook.presto.sql.planner.assertions.PlanMatchPattern.equiJoinClause;
import static com.facebook.presto.sql.planner.assertions.PlanMatchPattern.expression;
import static com.facebook.presto.sql.planner.assertions.PlanMatchPattern.functionCall;
import static com.facebook.presto.sql.planner.assertions.PlanMatchPattern.join;
import static com.facebook.presto.sql.planner.assertions.PlanMatchPattern.project;
import static com.facebook.presto.sql.planner.assertions.PlanMatchPattern.values;
import static com.facebook.presto.sql.planner.iterative.rule.test.PlanBuilder.expressions;
import static com.facebook.presto.sql.planner.plan.AggregationNode.Step.SINGLE;

public class TestPushAggregationThroughOuterJoin
{
    @Test
    public void testPushesAggregationThroughLeftJoin()
    {
        FunctionCall averageFunctionCall = new FunctionCall(QualifiedName.of("avg"), ImmutableList.of(new SymbolReference("COL2")));
        Signature avgSignature = new Signature(
                "avg",
                FunctionKind.AGGREGATE,
                ImmutableList.of(),
                ImmutableList.of(),
                DOUBLE.getTypeSignature(),
                ImmutableList.of(DOUBLE.getTypeSignature()),
                false);
        new RuleTester().assertThat(new PushAggregationThroughOuterJoin())
                .on(p ->
                        p.aggregation(
                                p.join(
                                        JoinNode.Type.LEFT,
                                        p.values(ImmutableList.of(p.symbol("COL1", BIGINT)), ImmutableList.of(expressions("10"))),
                                        p.values(p.symbol("COL2", BIGINT)),
                                        ImmutableList.of(new JoinNode.EquiJoinClause(p.symbol("COL1", BIGINT), p.symbol("COL2", BIGINT))),
                                        ImmutableList.of(p.symbol("COL1", BIGINT), p.symbol("COL2", BIGINT)),
                                        Optional.empty(),
                                        Optional.empty(),
                                        Optional.empty()
                                ),
                                ImmutableMap.of(p.symbol("AVG", DOUBLE),
                                        new AggregationNode.Aggregation(averageFunctionCall, avgSignature, Optional.empty())),
                                ImmutableList.of(ImmutableList.of(p.symbol("COL1", BIGINT))),
                                SINGLE,
                                Optional.empty(),
                                Optional.empty())
                )
                .matches(
                        project(ImmutableMap.of(
                                "COL1", expression("COL1"),
                                "COALESCE", expression("coalesce(AVG, AVG_NULL)")),
                                join(JoinNode.Type.INNER, ImmutableList.of(),
                                        join(JoinNode.Type.LEFT, ImmutableList.of(equiJoinClause("COL1", "COL2")),
                                                values(ImmutableMap.of("COL1", 0)),
                                                aggregation(
                                                        ImmutableList.of(ImmutableList.of("COL2")),
                                                        ImmutableMap.of(Optional.of("AVG"), functionCall("avg", ImmutableList.of("COL2"))),
                                                        ImmutableMap.of(),
                                                        Optional.empty(),
                                                        SINGLE,
                                                        values(ImmutableMap.of("COL2", 0)))),
                                        aggregation(
                                                ImmutableList.of(ImmutableList.of()),
                                                ImmutableMap.of(Optional.of("AVG_NULL"), functionCall("avg", ImmutableList.of("null_literal"))),
                                                ImmutableMap.of(),
                                                Optional.empty(),
                                                SINGLE,
                                                values(ImmutableMap.of("null_literal", 0))))));
    }

    @Test
    public void testPushesAggregationThroughRightJoin()
    {
        FunctionCall averageFunctionCall = new FunctionCall(QualifiedName.of("avg"), ImmutableList.of(new SymbolReference("COL2")));
        Signature avgSignature = new Signature(
                "avg",
                FunctionKind.AGGREGATE,
                ImmutableList.of(),
                ImmutableList.of(),
                DOUBLE.getTypeSignature(),
                ImmutableList.of(DOUBLE.getTypeSignature()),
                false);
        new RuleTester().assertThat(new PushAggregationThroughOuterJoin())
                .on(p ->
                        p.aggregation(
                                p.join(
                                        JoinNode.Type.RIGHT,
                                        p.values(p.symbol("COL2", BIGINT)),
                                        p.values(ImmutableList.of(p.symbol("COL1", BIGINT)), ImmutableList.of(expressions("10"))),
                                        ImmutableList.of(new JoinNode.EquiJoinClause(p.symbol("COL2", BIGINT), p.symbol("COL1", BIGINT))),
                                        ImmutableList.of(p.symbol("COL2", BIGINT), p.symbol("COL1", BIGINT)),
                                        Optional.empty(),
                                        Optional.empty(),
                                        Optional.empty()
                                ),
                                ImmutableMap.of(p.symbol("AVG", DOUBLE), new AggregationNode.Aggregation(averageFunctionCall, avgSignature, Optional.empty())),
                                ImmutableList.of(ImmutableList.of(p.symbol("COL1", BIGINT))),
                                SINGLE,
                                Optional.empty(),
                                Optional.empty())
                )
                .matches(
                        project(ImmutableMap.of(
                                "COALESCE", expression("coalesce(AVG, AVG_NULL)"),
                                "COL1", expression("COL1")),
                                join(JoinNode.Type.INNER, ImmutableList.of(),
                                        join(JoinNode.Type.RIGHT, ImmutableList.of(equiJoinClause("COL2", "COL1")),
                                                aggregation(
                                                        ImmutableList.of(ImmutableList.of("COL2")),
                                                        ImmutableMap.of(Optional.of("AVG"), functionCall("avg", ImmutableList.of("COL2"))),
                                                        ImmutableMap.of(),
                                                        Optional.empty(),
                                                        SINGLE,
                                                        values(ImmutableMap.of("COL2", 0))),
                                                values(ImmutableMap.of("COL1", 0))),
                                        aggregation(
                                                ImmutableList.of(ImmutableList.of()),
                                                ImmutableMap.of(
                                                        Optional.of("AVG_NULL"), functionCall("avg", ImmutableList.of("null_literal"))),
                                                ImmutableMap.of(),
                                                Optional.empty(),
                                                SINGLE,
                                                values(ImmutableMap.of("null_literal", 0))))));
    }

    @Test
    public void testDoesNotFireWhenNotDistinct()
    {
        FunctionCall averageFunctionCall = new FunctionCall(QualifiedName.of("avg"), ImmutableList.of(new SymbolReference("COL2")));
        Signature avgSignature = new Signature(
                "avg",
                FunctionKind.AGGREGATE,
                ImmutableList.of(),
                ImmutableList.of(),
                DOUBLE.getTypeSignature(),
                ImmutableList.of(DOUBLE.getTypeSignature()),
                false);
        new RuleTester().assertThat(new PushAggregationThroughOuterJoin())
                .on(p ->
                        p.aggregation(
                                p.join(
                                        JoinNode.Type.LEFT,
                                        p.values(ImmutableList.of(p.symbol("COL1", BIGINT)), ImmutableList.of(expressions("10"), expressions("11"))),
                                        p.values(new Symbol("COL2")),
                                        ImmutableList.of(new JoinNode.EquiJoinClause(new Symbol("COL1"), new Symbol("COL2"))),
                                        ImmutableList.of(new Symbol("COL1"), new Symbol("COL2")),
                                        Optional.empty(),
                                        Optional.empty(),
                                        Optional.empty()
                                ),
                                ImmutableMap.of(new Symbol("AVG"),
                                        new AggregationNode.Aggregation(averageFunctionCall, avgSignature, Optional.empty())),
                                ImmutableList.of(ImmutableList.of(new Symbol("COL1"))),
                                SINGLE,
                                Optional.empty(),
                                Optional.empty())
                )
                .doesNotFire();
    }

    @Test
    public void testDoesNotFireWhenGroupingOnInner()
    {
        FunctionCall averageFunctionCall = new FunctionCall(QualifiedName.of("avg"), ImmutableList.of(new SymbolReference("COL2")));
        Signature avgSignature = new Signature(
                "avg",
                FunctionKind.AGGREGATE,
                ImmutableList.of(),
                ImmutableList.of(),
                DOUBLE.getTypeSignature(),
                ImmutableList.of(DOUBLE.getTypeSignature()),
                false);
        new RuleTester().assertThat(new PushAggregationThroughOuterJoin())
                .on(p ->
                        p.aggregation(
                                p.join(
                                        JoinNode.Type.LEFT,
                                        p.values(ImmutableList.of(p.symbol("COL1", BIGINT)), ImmutableList.of(expressions("10"))),
                                        p.values(new Symbol("COL2"), new Symbol("COL3")),
                                        ImmutableList.of(new JoinNode.EquiJoinClause(new Symbol("COL1"), new Symbol("COL2"))),
                                        ImmutableList.of(new Symbol("COL1"), new Symbol("COL2")),
                                        Optional.empty(),
                                        Optional.empty(),
                                        Optional.empty()
                                ),
                                ImmutableMap.of(new Symbol("AVG"),
                                        new AggregationNode.Aggregation(averageFunctionCall, avgSignature, Optional.empty())),
                                ImmutableList.of(ImmutableList.of(new Symbol("COL1"), new Symbol("COL3"))),
                                SINGLE,
                                Optional.empty(),
                                Optional.empty())
                )
                .doesNotFire();
    }
}
