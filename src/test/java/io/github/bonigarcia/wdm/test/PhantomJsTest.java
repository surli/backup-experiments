/*
 * (C) Copyright 2016 Boni Garcia (http://bonigarcia.github.io/)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 */
package io.github.bonigarcia.wdm.test;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.openqa.selenium.phantomjs.PhantomJSDriver;

import io.github.bonigarcia.wdm.PhantomJsDriverManager;
import io.github.bonigarcia.wdm.base.BaseBrowserTst;

/**
 * Test with PhatomJS.
 *
 * @author Boni Garcia (boni.gg@gmail.com)
 * @since 1.4.0
 */

@Ignore("Latest PhantomJS version (2.5.0-beta) is buggy for Linux at this moment")
public class PhantomJsTest extends BaseBrowserTst {

	@BeforeClass
	public static void setupClass() {
		PhantomJsDriverManager.getInstance().setup();
	}

	@Before
	public void setupTest() {
		driver = new PhantomJSDriver();
	}

}
