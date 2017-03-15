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

package com.facebook.presto.spi.block;

import org.testng.annotations.Test;

import java.lang.invoke.MethodHandle;

import static com.facebook.presto.spi.block.MethodHandleUtil.compose;
import static com.facebook.presto.spi.block.MethodHandleUtil.methodHandle;
import static org.testng.Assert.assertEquals;

public class TestMethodHandleUtil
{
    @Test
    public void testCompose2()
            throws Throwable
    {
        MethodHandle squareBracket = methodHandle(TestMethodHandleUtil.class, "squareBracket", String.class);
        MethodHandle curlyBracket = methodHandle(TestMethodHandleUtil.class, "curlyBracket", String.class, char.class);
        MethodHandle composed = compose(squareBracket, curlyBracket);
        assertEquals((String) composed.invokeExact("str", '%'), "[{str=%}]");
    }

    @Test
    public void testCompose3()
            throws Throwable
    {
        MethodHandle squareBracket = methodHandle(TestMethodHandleUtil.class, "squareBracket", String.class, double.class);
        MethodHandle curlyBracket = methodHandle(TestMethodHandleUtil.class, "curlyBracket", String.class, char.class);
        MethodHandle singleQuote = methodHandle(TestMethodHandleUtil.class, "sum", long.class, int.class);
        MethodHandle composed = compose(squareBracket, curlyBracket, singleQuote);
        assertEquals((String) composed.invokeExact("str", '%', 9876543210L, 123), "[{str=%},9876543333]");
    }

    public static String squareBracket(String s)
    {
        return "[" + s + "]";
    }

    public static String squareBracket(String s, double d)
    {
        return "[" + s + "," + ((long) d) + "]";
    }

    public static String curlyBracket(String s, char c)
    {
        return "{" + s + "=" + c + "}";
    }

    public static double sum(long x, int c)
    {
        return (double) x + c;
    }
}
