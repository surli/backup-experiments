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

import com.facebook.presto.sql.planner.assertions.ExpressionMatcher;
import com.facebook.presto.sql.planner.assertions.PlanMatchPattern;
import com.facebook.presto.sql.planner.iterative.rule.test.RuleTester;
import com.facebook.presto.sql.planner.plan.Assignments;
import com.google.common.collect.ImmutableMap;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.facebook.presto.spi.type.BigintType.BIGINT;
import static com.facebook.presto.sql.planner.assertions.PlanMatchPattern.project;
import static com.facebook.presto.sql.planner.assertions.PlanMatchPattern.values;
import static com.facebook.presto.sql.planner.iterative.rule.test.PlanBuilder.expression;
import static io.airlift.testing.Closeables.closeAllRuntimeException;

public class TestInlineProjections
{
    private RuleTester tester;

    @BeforeClass
    public void setUp()
    {
        tester = new RuleTester();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown()
    {
        closeAllRuntimeException(tester);
        tester = null;
    }

    @Test
    public void test()
    {
        tester.assertThat(new InlineProjections())
                .on(p ->
                        p.project(
                                Assignments.builder()
                                        .put(p.symbol("identity", BIGINT), expression("symbol")) // identity
                                        .put(p.symbol("multi_complex_1", BIGINT), expression("complex + 1")) // complex expression referenced multiple times
                                        .put(p.symbol("multi_complex_2", BIGINT), expression("complex + 2")) // complex expression referenced multiple times
                                        .put(p.symbol("multi_literal_1", BIGINT), expression("literal + 1")) // literal referenced multiple times
                                        .put(p.symbol("multi_literal_2", BIGINT), expression("literal + 2")) // literal referenced multiple times
                                        .put(p.symbol("single_complex", BIGINT), expression("complex_2 + 2")) // complex expression reference only once
                                        .put(p.symbol("try", BIGINT), expression("try(complex / literal)"))
                                        .build(),
                                p.project(Assignments.builder()
                                                .put(p.symbol("symbol", BIGINT), expression("x"))
                                                .put(p.symbol("complex", BIGINT), expression("x * 2"))
                                                .put(p.symbol("literal", BIGINT), expression("1"))
                                                .put(p.symbol("complex_2", BIGINT), expression("x - 1"))
                                                .build(),
                                        p.values(p.symbol("x", BIGINT)))))
                .matches(
                        project(
                                ImmutableMap.<String, ExpressionMatcher>builder()
                                        .put("out1", PlanMatchPattern.expression("x"))
                                        .put("out2", PlanMatchPattern.expression("y + 1"))
                                        .put("out3", PlanMatchPattern.expression("y + 2"))
                                        .put("out4", PlanMatchPattern.expression("1 + 1"))
                                        .put("out5", PlanMatchPattern.expression("1 + 2"))
                                        .put("out6", PlanMatchPattern.expression("x - 1 + 2"))
                                        .put("out7", PlanMatchPattern.expression("try(y / 1)"))
                                        .build(),
                                project(
                                        ImmutableMap.of(
                                                "x", PlanMatchPattern.expression("x"),
                                                "y", PlanMatchPattern.expression("x * 2")),
                                        values(ImmutableMap.of("x", 0)))));
    }

    @Test
    public void testIdentityProjections()
            throws Exception
    {
        tester.assertThat(new InlineProjections())
                .on(p ->
                        p.project(
                                Assignments.of(p.symbol("output", BIGINT), expression("value")),
                                p.project(
                                        Assignments.identity(p.symbol("value", BIGINT)),
                                        p.values(p.symbol("value", BIGINT)))))
                .doesNotFire();
    }
}
