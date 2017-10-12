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

import java.time.LocalTime;
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

public class LocalTimePropertyParameterTypesTest {
    @Test public void localTime() {
        assertThat(testResult(LocalTimes.class), isSuccessful());
    }

    @RunWith(JUnitQuickcheck.class)
    public static class LocalTimes {
        @Property public void shouldHold(LocalTime t) {
        }
    }

    @Test public void rangedLocalTime() {
        assertThat(testResult(RangedLocalTime.class), isSuccessful());
    }

    @RunWith(JUnitQuickcheck.class)
    public static class RangedLocalTime {
        @Property public void shouldHold(
            @InRange(
                min = "00:00:00.0",
                max = "23:59:59.999999999",
                format = "HH:mm:ss.n")
            LocalTime t) {

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.n");

            assertThat(
                t,
                allOf(
                    greaterThanOrEqualTo(LocalTime.parse("00:00:00.0", formatter)),
                    lessThanOrEqualTo(LocalTime.parse("23:59:59.999999999", formatter))));
        }
    }

    @Test public void malformedMin() {
        assertThat(
            testResult(MalformedMinLocalTime.class),
            hasSingleFailureContaining(DateTimeParseException.class.getName()));
    }

    @RunWith(JUnitQuickcheck.class)
    public static class MalformedMinLocalTime {
        @Property public void shouldHold(
            @InRange(
                min = "@#!@#@",
                max = "13:45.30.123456789",
                format = "HH:mm:ss.n")
            LocalTime t) {
        }
    }

    @Test public void malformedMax() {
        assertThat(
            testResult(MalformedMaxLocalTime.class),
            hasSingleFailureContaining(DateTimeParseException.class.getName()));
    }

    @RunWith(JUnitQuickcheck.class)
    public static class MalformedMaxLocalTime {
        @Property public void shouldHold(
            @InRange(
                min = "01:15:23.23929",
                max = "*&@^#%$",
                format = "HH:mm:ss.n")
            LocalTime t) {
        }
    }

    @Test public void malformedFormat() {
        assertThat(
            testResult(MalformedFormatLocalTime.class),
            hasSingleFailureContaining(IllegalArgumentException.class.getName()));
    }

    @RunWith(JUnitQuickcheck.class)
    public static class MalformedFormatLocalTime {
        @Property public void shouldHold(
            @InRange(
                min = "00:00:00.0",
                max = "23:59:59.999999999",
                format = "*@&^#$")
            LocalTime t) {
        }
    }

    @Test public void missingMin() {
        assertThat(testResult(MissingMin.class), isSuccessful());
    }

    @RunWith(JUnitQuickcheck.class)
    public static class MissingMin {
        @Property public void shouldHold(
            @InRange(max = "23:59:59.999999999", format = "HH:mm:ss.n") LocalTime t) {

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.n");
            assertThat(
                t,
                lessThanOrEqualTo(LocalTime.parse("23:59:59.999999999", formatter)));
        }
    }

    @Test public void missingMax() {
        assertThat(testResult(MissingMax.class), isSuccessful());
    }

    @RunWith(JUnitQuickcheck.class)
    public static class MissingMax {
        @Property public void shouldHold(
            @InRange(min = "23:59:59.999999999", format = "HH:mm:ss.n") LocalTime t) {

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.n");
            assertThat(
                t,
                greaterThanOrEqualTo(LocalTime.parse("23:59:59.999999999", formatter)));
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
                min = "23:59:59.999999999",
                max = "00:00:00.0",
                format = "HH:mm:ss.n")
            LocalTime t) {
        }
    }
}
