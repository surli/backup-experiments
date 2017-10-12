/*
 The MIT License

 Copyright (c) 2010-2017 Paul R. Holser, Jr.

 Permission is hereby granted, free of charge, to any person obtaining
 a copy of this software and associated documentation files (the
 "Software"), to deal in the Software without restriction, including
 without limitation the rights to use, copy, modify, merge, publish,
 distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to
 the following conditions:

 The above copyright notice and this permission notice shall be
 included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package com.pholser.junit.quickcheck.generator.java.util;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.junit.Assume.*;
import static org.junit.experimental.results.PrintableResult.*;
import static org.junit.experimental.results.ResultMatchers.*;

public class OptionalDoublePropertyParameterTest {
    @Test public void maybeADouble() {
        assertThat(testResult(MaybeADouble.class), isSuccessful());
    }

    @RunWith(JUnitQuickcheck.class)
    public static class MaybeADouble {
        @Property public void works(@InRange(maxDouble = 0.5) OptionalDouble d) {
            assumeTrue(d.isPresent());
            assertThat(d.getAsDouble(), lessThanOrEqualTo(0.5));
        }
    }

    @Test public void shrinking() {
        assertThat(testResult(ShrinkingOptionalDouble.class), failureCountIs(1));
        assertTrue(ShrinkingOptionalDouble.failed.stream().anyMatch(o -> !o.isPresent()));
    }

    @RunWith(JUnitQuickcheck.class)
    public static class ShrinkingOptionalDouble {
        static List<OptionalDouble> failed = new ArrayList<>();

        @Property public void works(OptionalDouble optional) {
            failed.add(optional);

            fail();
        }
    }
}
