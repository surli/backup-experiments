/*
 * Copyright 2014 the original author or authors.
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
package org.springframework.hateoas;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.springframework.hateoas.TemplateVariable.VariableType.*;

import java.util.List;

import org.junit.Test;
import org.springframework.hateoas.TemplateVariable.VariableType;

/**
 * Unit tests for {@link TemplateVariables}.
 * 
 * @author Oliver Gierke
 */
public class TemplateVariablesUnitTest {

	/**
	 * @see #137
	 */
	@Test
	public void rendersNoTempalteVariablesAsEmptyString() {
		assertThat(TemplateVariables.NONE.toString(), is(""));
	}

	/**
	 * @see #137
	 */
	@Test
	public void rendersSingleVariableCorrectly() {

		TemplateVariables variables = new TemplateVariables(new TemplateVariable("foo", SEGMENT));
		assertThat(variables.toString(), is("{/foo}"));
	}

	/**
	 * @see #137
	 */
	@Test
	public void combinesMultipleVariablesOfTheSameType() {

		TemplateVariable first = new TemplateVariable("foo", REQUEST_PARAM);
		TemplateVariable second = new TemplateVariable("bar", REQUEST_PARAM);

		TemplateVariables variables = new TemplateVariables(first, second);

		assertThat(variables.toString(), is("{?foo,bar}"));
	}

	/**
	 * @see #137
	 */
	@Test
	public void combinesMultipleVariablesOfTheDifferentType() {

		TemplateVariable first = new TemplateVariable("foo", SEGMENT);
		TemplateVariable second = new TemplateVariable("bar", REQUEST_PARAM);

		TemplateVariables variables = new TemplateVariables(first, second);

		assertThat(variables.toString(), is("{/foo}{?bar}"));
	}

	/**
	 * @see #137
	 */
	@Test
	public void concatsVariables() {

		TemplateVariables variables = new TemplateVariables(new TemplateVariable("foo", SEGMENT));
		variables = variables.concat(new TemplateVariable("bar", REQUEST_PARAM));

		assertThat(variables.toString(), is("{/foo}{?bar}"));
	}

	/**
	 * @see #137
	 */
	@Test
	public void combinesContinuedParamWithParam() {

		TemplateVariable first = new TemplateVariable("foo", REQUEST_PARAM);
		TemplateVariable second = new TemplateVariable("bar", REQUEST_PARAM_CONTINUED);

		TemplateVariables variables = new TemplateVariables(first, second);

		assertThat(variables.toString(), is("{?foo,bar}"));
	}

	/**
	 * @see #137
	 */
	@Test
	public void combinesContinuedParameterWithParameter() {

		TemplateVariable first = new TemplateVariable("foo", REQUEST_PARAM_CONTINUED);
		TemplateVariable second = new TemplateVariable("bar", REQUEST_PARAM);

		TemplateVariables variables = new TemplateVariables(first, second);

		assertThat(variables.toString(), is("{&foo,bar}"));
	}

	/**
	 * @see #198
	 */
	@Test
	public void dropsDuplicateTemplateVariable() {

		TemplateVariable variable = new TemplateVariable("foo", REQUEST_PARAM);
		TemplateVariables variables = new TemplateVariables(variable);

		List<TemplateVariable> result = variables.concat(variable).asList();

		assertThat(result, hasSize(1));
		assertThat(result, hasItem(variable));
	}

	/**
	 * @see #217
	 */
	@Test
	public void considersRequestParameterVariablesEquivalent() {

		TemplateVariable parameter = new TemplateVariable("foo", REQUEST_PARAM);
		TemplateVariable continued = new TemplateVariable("foo", REQUEST_PARAM_CONTINUED);
		TemplateVariable fragment = new TemplateVariable("foo", FRAGMENT);

		assertThat(parameter.isEquivalent(continued), is(true));
		assertThat(continued.isEquivalent(parameter), is(true));
		assertThat(fragment.isEquivalent(continued), is(false));
	}

	/**
	 * @see #217
	 */
	@Test
	public void considersFragementVariable() {

		assertThat(new TemplateVariable("foo", VariableType.FRAGMENT).isFragment(), is(true));
		assertThat(new TemplateVariable("foo", VariableType.REQUEST_PARAM).isFragment(), is(false));
	}

	/**
	 * @see #217
	 */
	@Test
	public void doesNotAddEquivalentVariable() {

		TemplateVariable parameter = new TemplateVariable("foo", VariableType.REQUEST_PARAM);
		TemplateVariable parameterContinued = new TemplateVariable("foo", VariableType.REQUEST_PARAM_CONTINUED);

		List<TemplateVariable> result = new TemplateVariables(parameter).concat(parameterContinued).asList();

		assertThat(result, hasSize(1));
		assertThat(result, hasItem(parameter));
	}

	/**
	 * @see #228
	 */
	@Test(expected = IllegalArgumentException.class)
	public void variableRejectsEmptyName() {
		new TemplateVariable("", PATH_VARIABLE);
	}

	/**
	 * @see #228
	 */
	@Test(expected = IllegalArgumentException.class)
	public void variableRejectsNullName() {
		new TemplateVariable(null, PATH_VARIABLE);
	}

	/**
	 * @see #228
	 */
	@Test(expected = IllegalArgumentException.class)
	public void variableRejectsNullType() {
		new TemplateVariable("foo", null);
	}

	/**
	 * @see #228
	 */
	@Test(expected = IllegalArgumentException.class)
	public void variableRejectsNullDescription() {
		new TemplateVariable("foo", PATH_VARIABLE, null);
	}
}
