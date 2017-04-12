/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.metron.common.stellar.evaluators;

import org.apache.metron.common.dsl.Token;
import org.apache.metron.common.stellar.generated.StellarParser;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class FloatLiteralEvaluatorTest {
  @Rule
  public final ExpectedException exception = ExpectedException.none();

  NumberEvaluator<StellarParser.FloatLiteralContext> evaluator;
  StellarParser.FloatLiteralContext context;

  @Before
  public void setUp() throws Exception {
    evaluator = new FloatLiteralEvaluator();
    context = mock(StellarParser.FloatLiteralContext.class);
  }

  @Test
  public void verifyHappyPathEvaluation() throws Exception {
    when(context.getText()).thenReturn("100f");

    Token<? extends Number> evaluated = evaluator.evaluate(context);
    assertEquals(new Token<>(100f, Float.class), evaluated);

    verify(context).getText();
    verifyNoMoreInteractions(context);
  }

  @Test
  public void verifyNumberFormationExceptionWithEmptyString() throws Exception {
    exception.expect(NumberFormatException.class);

    when(context.getText()).thenReturn("");
    evaluator.evaluate(context);
  }

  @Test
  public void throwIllegalArgumentExceptionWhenContextIsNull() throws Exception {
    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Cannot evaluate a context that is null.");

    evaluator.evaluate(null);
  }

}
