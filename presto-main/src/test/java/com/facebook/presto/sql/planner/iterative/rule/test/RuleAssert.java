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
package com.facebook.presto.sql.planner.iterative.rule.test;

import com.facebook.presto.Session;
import com.facebook.presto.cost.CostCalculator;
import com.facebook.presto.cost.PlanNodeCost;
import com.facebook.presto.metadata.Metadata;
import com.facebook.presto.security.AccessControl;
import com.facebook.presto.spi.type.Type;
import com.facebook.presto.sql.planner.Plan;
import com.facebook.presto.sql.planner.PlanNodeIdAllocator;
import com.facebook.presto.sql.planner.Symbol;
import com.facebook.presto.sql.planner.SymbolAllocator;
import com.facebook.presto.sql.planner.assertions.PlanMatchPattern;
import com.facebook.presto.sql.planner.iterative.Lookup;
import com.facebook.presto.sql.planner.iterative.Memo;
import com.facebook.presto.sql.planner.iterative.Rule;
import com.facebook.presto.sql.planner.plan.PlanNode;
import com.facebook.presto.sql.planner.plan.PlanNodeId;
import com.facebook.presto.sql.planner.planPrinter.PlanPrinter;
import com.facebook.presto.transaction.TransactionManager;
import com.google.common.collect.ImmutableSet;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static com.facebook.presto.sql.planner.assertions.PlanAssert.assertPlan;
import static com.facebook.presto.transaction.TransactionBuilder.transaction;
import static com.google.common.base.Preconditions.checkArgument;
import static org.testng.Assert.fail;

public class RuleAssert
{
    private final Metadata metadata;
    private final CostCalculator costCalculator;
    private Session session;
    private final Rule rule;

    private final PlanNodeIdAllocator idAllocator = new PlanNodeIdAllocator();

    private Map<Symbol, Type> symbols;
    private PlanNode plan;
    private final TransactionManager transactionManager;
    private final AccessControl accessControl;

    public RuleAssert(Metadata metadata, CostCalculator costCalculator, Session session, Rule rule, TransactionManager transactionManager, AccessControl accessControl)
    {
        this.metadata = metadata;
        this.costCalculator = costCalculator;
        this.session = session;
        this.rule = rule;
        this.transactionManager = transactionManager;
        this.accessControl = accessControl;
    }

    public RuleAssert setSystemProperty(String key, String value)
    {
        return withSession(Session.builder(session)
                .setSystemProperty(key, value)
                .build());
    }

    public RuleAssert withSession(Session session)
    {
        this.session = session;
        return this;
    }

    public RuleAssert on(Function<PlanBuilder, PlanNode> planProvider)
    {
        checkArgument(plan == null, "plan has already been set");

        PlanBuilder builder = new PlanBuilder(idAllocator, metadata);
        plan = planProvider.apply(builder);
        symbols = builder.getSymbols();
        return this;
    }

    public void doesNotFire()
    {
        SymbolAllocator symbolAllocator = new SymbolAllocator(symbols);
        Optional<PlanNode> result = inTransaction(session -> rule.apply(plan, x -> x, idAllocator, symbolAllocator, session));

        if (result.isPresent()) {
            fail(String.format(
                    "Expected %s to not fire for:\n%s",
                    rule.getClass().getName(),
                    inTransaction(session -> PlanPrinter.textLogicalPlan(plan, symbolAllocator.getTypes(), metadata, costCalculator, session, 2))));
        }
    }

    public void matches(PlanMatchPattern pattern)
    {
        SymbolAllocator symbolAllocator = new SymbolAllocator(symbols);
        Memo memo = new Memo(idAllocator, plan);
        Lookup lookup = Lookup.from(memo::resolve);
        Optional<PlanNode> result = inTransaction(session -> rule.apply(memo.getNode(memo.getRootGroup()), lookup, idAllocator, symbolAllocator, session));
        Map<Symbol, Type> types = symbolAllocator.getTypes();

        if (!result.isPresent()) {
            fail(String.format(
                    "%s did not fire for:\n%s",
                    rule.getClass().getName(),
                    inTransaction(session -> PlanPrinter.textLogicalPlan(plan, types, metadata, costCalculator, session, 2))));
        }

        PlanNode actual = result.get();

        if (actual == plan) { // plans are not comparable, so we can only ensure they are not the same instance
            fail(String.format(
                    "%s: rule fired but return the original plan:\n%s",
                    rule.getClass().getName(),
                    inTransaction(session -> PlanPrinter.textLogicalPlan(plan, types, metadata, costCalculator, session, 2))));
        }

        if (!ImmutableSet.copyOf(plan.getOutputSymbols()).equals(ImmutableSet.copyOf(actual.getOutputSymbols()))) {
            fail(String.format(
                    "%s: output schema of transformed and original plans are not equivalent\n" +
                            "\texpected: %s\n" +
                            "\tactual:   %s",
                    rule.getClass().getName(),
                    plan.getOutputSymbols(),
                    actual.getOutputSymbols()));
        }

        inTransaction(session -> {
            Map<PlanNodeId, PlanNodeCost> planNodeCosts = costCalculator.calculateCostForPlan(session, types, actual);
            assertPlan(session, metadata, costCalculator, new Plan(actual, types, planNodeCosts), lookup, pattern);
            return null;
        });
    }

    private <T> T inTransaction(Function<Session, T> transactionSessionConsumer)
    {
        return transaction(transactionManager, accessControl)
                .singleStatement()
                .execute(session, session -> {
                    // metadata.getCatalogHandle() registers the catalog for the transaction
                    session.getCatalog().ifPresent(catalog -> metadata.getCatalogHandle(session, catalog));
                    return transactionSessionConsumer.apply(session);
                });
    }
}
