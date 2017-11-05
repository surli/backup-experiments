/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.jackrabbit.oak.commons;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.google.common.base.Charsets;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.apache.jackrabbit.oak.commons.FileIOUtils.lexComparator;
import static org.apache.jackrabbit.oak.commons.FileIOUtils.lineBreakAwareComparator;
import static org.apache.jackrabbit.oak.commons.FileIOUtils.readStringsAsSet;
import static org.apache.jackrabbit.oak.commons.FileIOUtils.sort;
import static org.apache.jackrabbit.oak.commons.FileIOUtils.writeStrings;
import static org.apache.jackrabbit.oak.commons.sort.EscapeUtils.escapeLineBreak;
import static org.apache.jackrabbit.oak.commons.sort.EscapeUtils.unescapeLineBreaks;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;


/**
 * Tests for {@link FileIOUtils}
 */
public class FileIOUtilsTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder(new File("./target"));

    private static final Random RANDOM = new Random();

    @Test
    public void writeReadStrings() throws Exception {
        Set<String> added = newHashSet("a", "z", "e", "b");
        File f = folder.newFile();

        int count = writeStrings(added.iterator(), f, false);
        assertEquals(added.size(), count);

        Set<String> retrieved = readStringsAsSet(new FileInputStream(f), false);

        assertEquals(added, retrieved);
    }

    @Test
    public void writeReadStringsWithLineBreaks() throws IOException {
        Set<String> added = newHashSet(getLineBreakStrings());
        File f = folder.newFile();
        int count = writeStrings(added.iterator(), f, true);
        assertEquals(added.size(), count);

        Set<String> retrieved = readStringsAsSet(new FileInputStream(f), true);
        assertEquals(added, retrieved);
    }

    @Test
    public void writeReadRandomStrings() throws Exception {
        Set<String> added = newHashSet();
        File f = folder.newFile();

        for (int i = 0; i < 100; i++) {
            added.add(getRandomTestString());
        }
        int count = writeStrings(added.iterator(), f, true);
        assertEquals(added.size(), count);

        Set<String> retrieved = readStringsAsSet(new FileInputStream(f), true);
        assertEquals(added, retrieved);
    }

    @Test
    public void compareWithLineBreaks() throws Exception {
        Comparator<String> cmp = lineBreakAwareComparator(lexComparator);

        List<String> strs = getLineBreakStrings();
        Collections.sort(strs);

        // Escape line breaks and then compare with string sorted
        List<String> escapedStrs = escape(getLineBreakStrings());
        Collections.sort(escapedStrs, cmp);

        assertEquals(strs, unescape(escapedStrs));
    }

    @Test
    public void sortTest() throws IOException {
        List<String> list = newArrayList("a", "z", "e", "b");
        File f = folder.newFile();
        writeStrings(list.iterator(), f, false);
        sort(f);

        BufferedReader reader =
            new BufferedReader(new InputStreamReader(new FileInputStream(f), Charsets.UTF_8));
        String line = null;
        List<String> retrieved = newArrayList();
        while ((line = reader.readLine()) != null) {
            retrieved.add(line);
        }
        IOUtils.closeQuietly(reader);
        Collections.sort(list);
        assertArrayEquals(Arrays.toString(list.toArray()), list.toArray(), retrieved.toArray());
    }

    @Test
    public void sortCustomComparatorTest() throws IOException {
        List<String> list = getLineBreakStrings();
        File f = folder.newFile();
        writeStrings(list.iterator(), f, true);
        sort(f, lineBreakAwareComparator(lexComparator));

        BufferedReader reader =
            new BufferedReader(new InputStreamReader(new FileInputStream(f), Charsets.UTF_8));
        String line = null;
        List<String> retrieved = newArrayList();
        while ((line = reader.readLine()) != null) {
            retrieved.add(unescapeLineBreaks(line));
        }
        IOUtils.closeQuietly(reader);
        Collections.sort(list);
        assertArrayEquals(Arrays.toString(list.toArray()), list.toArray(), retrieved.toArray());
    }

    private static List<String> getLineBreakStrings() {
        return newArrayList("ab\nc\r", "ab\\z", "a\\\\z\nc",
            "/a", "/a/b\nc", "/a/b\rd", "/a/b\r\ne", "/a/c");
    }

    private static List<String> escape(List<String> list) {
        List<String> escaped = newArrayList();
        for (String s : list) {
            escaped.add(escapeLineBreak(s));
        }
        return escaped;
    }

    private static List<String> unescape(List<String> list) {
        List<String> unescaped = newArrayList();
        for (String s : list) {
            unescaped.add(unescapeLineBreaks(s));
        }
        return unescaped;
    }

    private static String getRandomTestString() throws Exception {
        boolean valid = false;
        StringBuilder buffer = new StringBuilder();
        while(!valid) {
            int length = RANDOM.nextInt(40);
            for (int i = 0; i < length; i++) {
                buffer.append((char) (RANDOM.nextInt(Character.MAX_VALUE)));
            }
            String s = buffer.toString();
            CharsetEncoder encoder = Charset.forName(Charsets.UTF_8.toString()).newEncoder();
            try {
                encoder.encode(CharBuffer.wrap(s));
                valid = true;
            } catch (CharacterCodingException e) {
                buffer = new StringBuilder();
            }
        }
        return buffer.toString();
    }
}
