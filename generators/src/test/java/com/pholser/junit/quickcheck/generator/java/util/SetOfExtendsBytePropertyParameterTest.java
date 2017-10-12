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

import java.util.List;
import java.util.Set;

import com.pholser.junit.quickcheck.Generating;
import com.pholser.junit.quickcheck.generator.BasicGeneratorPropertyParameterTest;

import static com.google.common.collect.Sets.*;
import static com.pholser.junit.quickcheck.Generating.*;
import static java.util.Arrays.*;
import static org.mockito.Mockito.*;

public class SetOfExtendsBytePropertyParameterTest
    extends BasicGeneratorPropertyParameterTest {

    public static final Set<? extends Byte> TYPE_BEARER = null;

    @Override protected void primeSourceOfRandomness() {
        when(Generating.bytes(randomForParameterGenerator))
            .thenReturn((byte) 6).thenReturn((byte) 7).thenReturn((byte) 8);
        when(Generating.ints(randomForGeneratorRepo, eq(0), anyInt()))
            .thenReturn(0);
        when(distro.sampleWithMean(1, randomForParameterGenerator)).thenReturn(0);
        when(distro.sampleWithMean(2, randomForParameterGenerator)).thenReturn(1);
        when(distro.sampleWithMean(3, randomForParameterGenerator)).thenReturn(2);
    }

    @Override protected int trials() {
        return 3;
    }

    @SuppressWarnings("unchecked")
    @Override protected List<?> randomValues() {
        return asList(newHashSet(), newHashSet((byte) 6), newHashSet((byte) 7, (byte) 8));
    }

    @Override public void verifyInteractionWithRandomness() {
        verifyBytes(randomForParameterGenerator, times(3));
    }
}
