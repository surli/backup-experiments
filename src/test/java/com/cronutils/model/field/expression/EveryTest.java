/*
 * Copyright 2015 jmrozanec
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cronutils.model.field.expression;

import org.junit.Test;

import com.cronutils.model.field.value.IntegerFieldValue;

import static org.junit.Assert.assertEquals;

public class EveryTest {
    @Test
    public void testGetTime() {
        final int every = 5;
        assertEquals(every, (int) new Every(new IntegerFieldValue(every)).getPeriod().getValue());
    }

    @Test
    public void testGetTimeNull() {
        assertEquals(1, (int) new Every(null).getPeriod().getValue());
    }

    @Test //issue #180
    public void testAsString() {
        assertEquals("0/1", new Every(new On(new IntegerFieldValue(0)), new IntegerFieldValue(1)).asString());
    }
}