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

package com.pholser.junit.quickcheck;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.junit.experimental.results.PrintableResult.*;
import static org.junit.experimental.results.ResultMatchers.*;

public class OffsetDateTimePropertyParameterTypesTest {
    @Test public void offsetDateTime() {
        assertThat(testResult(OffsetDateTimes.class), isSuccessful());
    }

    @RunWith(JUnitQuickcheck.class)
    public static class OffsetDateTimes {
        @Property public void shouldHold(OffsetDateTime t) {
        }
    }

    @Test public void rangedOffsetDateTime() {
        assertThat(testResult(RangedOffsetDateTime.class), isSuccessful());
    }

    @RunWith(JUnitQuickcheck.class)
    public static class RangedOffsetDateTime {
        @Property public void shouldHold(
            @InRange(
                min = "01/01/2012T00:00:00.0+01:00",
                max = "12/31/2012T23:59:59.999999999+01:00",
                format = "MM/dd/yyyy'T'HH:mm:ss.nxxx")
            OffsetDateTime t) {

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy'T'HH:mm:ss.nxxx");

            assertThat(
                t,
                allOf(
                    greaterThanOrEqualTo(OffsetDateTime.parse("01/01/2012T00:00:00.0+01:00", formatter)),
                    lessThanOrEqualTo(OffsetDateTime.parse("12/31/2012T23:59:59.999999999+01:00", formatter))));
        }
    }

    @Test public void malformedMin() {
        assertThat(
            testResult(MalformedMinOffsetDateTime.class),
            hasSingleFailureContaining(DateTimeParseException.class.getName()));
    }

    @RunWith(JUnitQuickcheck.class)
    public static class MalformedMinOffsetDateTime {
        @Property public void shouldHold(
            @InRange(
                min = "@#!@#@",
                max = "12/31/2012T23:59:59.999999999+01:00",
                format = "MM/dd/yyyy'T'HH:mm:ss.nxxx")
            OffsetDateTime t) {
        }
    }

    @Test public void malformedMax() {
        assertThat(
            testResult(MalformedMaxOffsetDateTime.class),
            hasSingleFailureContaining(DateTimeParseException.class.getName()));
    }

    @RunWith(JUnitQuickcheck.class)
    public static class MalformedMaxOffsetDateTime {
        @Property public void shouldHold(
            @InRange(
                min = "06/01/2011T23:59:59.999999999+01:00",
                max = "*&@^#%$",
                format = "MM/dd/yyyy'T'HH:mm:ss.nxxx")
            OffsetDateTime t) {
        }
    }

    @Test public void malformedFormat() {
        assertThat(
            testResult(MalformedFormatOffsetDateTime.class),
            hasSingleFailureContaining(IllegalArgumentException.class.getName()));
    }

    @RunWith(JUnitQuickcheck.class)
    public static class MalformedFormatOffsetDateTime {
        @Property public void shouldHold(
            @InRange(
                min = "06/01/2011T23:59:59.999999999+01:00",
                max = "06/30/2011T23:59:59.999999999+01:00",
                format = "*@&^#$")
            OffsetDateTime t) {
        }
    }

    @Test public void missingMin() {
        assertThat(testResult(MissingMin.class), isSuccessful());
    }

    @RunWith(JUnitQuickcheck.class)
    public static class MissingMin {
        @Property public void shouldHold(
            @InRange(
                max = "12/31/2012T23:59:59.999999999+01:00",
                format = "MM/dd/yyyy'T'HH:mm:ss.nxxx")
            OffsetDateTime t) {

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy'T'HH:mm:ss.nxxx");
            assertThat(
                t,
                lessThanOrEqualTo(
                    OffsetDateTime.parse(
                        "12/31/2012T23:59:59.999999999+01:00",
                        formatter)));
        }
    }

    @Test public void missingMax() {
        assertThat(testResult(MissingMax.class), isSuccessful());
    }

    @RunWith(JUnitQuickcheck.class)
    public static class MissingMax {
        @Property public void shouldHold(
            @InRange(
                min = "12/31/2012T23:59:59.999999999+01:00",
                format = "MM/dd/yyyy'T'HH:mm:ss.nxxx")
            OffsetDateTime t) {

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy'T'HH:mm:ss.nxxx");
            assertThat(
                t,
                greaterThanOrEqualTo(
                    OffsetDateTime.parse("12/31/2012T23:59:59.999999999+01:00", formatter)));
        }
    }

    @Test public void backwardsRange() {
        assertThat(
            testResult(BackwardsRange.class),
            hasSingleFailureContaining(IllegalArgumentException.class.getName()));
    }

    @RunWith(JUnitQuickcheck.class)
    public static class BackwardsRange {
        @Property public void shouldHold(
            @InRange(
                min = "12/31/2012T23:59:59.999999999+01:00",
                max = "12/01/2012T00:00:00.0+01:00",
                format = "MM/dd/yyyy'T'HH:mm:ss.nxxx")
            OffsetDateTime t) {
        }
    }
}
