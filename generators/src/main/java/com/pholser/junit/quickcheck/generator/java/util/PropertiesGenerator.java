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

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.generator.java.lang.AbstractStringGenerator;
import com.pholser.junit.quickcheck.generator.java.lang.Encoded;
import com.pholser.junit.quickcheck.generator.java.lang.StringGenerator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

import static java.util.Arrays.*;

/**
 * Produces values of type {@link Properties}.
 */
public class PropertiesGenerator extends Generator<Properties> {
    private AbstractStringGenerator stringGenerator = new StringGenerator();

    public PropertiesGenerator() {
        super(Properties.class);
    }

    public void configure(Encoded.InCharset charset) {
        Encoded encoded = new Encoded();
        encoded.configure(charset);
        stringGenerator = encoded;
    }

    @Override public Properties generate(SourceOfRandomness random, GenerationStatus status) {
        int size = status.size();

        Properties properties = new Properties();
        for (int i = 0; i < size; ++i) {
            properties.setProperty(
                stringGenerator.generate(random, status),
                stringGenerator.generate(random, status));
        }

        return properties;
    }

    @SuppressWarnings("unchecked")
    @Override public boolean canRegisterAsType(Class<?> type) {
        Set<Class<?>> exclusions =
            new HashSet<>(asList(
                Object.class,
                Hashtable.class,
                Map.class,
                Dictionary.class));
        return !exclusions.contains(type);
    }
}
