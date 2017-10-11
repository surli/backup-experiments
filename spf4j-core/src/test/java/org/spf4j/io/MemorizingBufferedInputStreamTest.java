/*
 * Copyright (c) 2001-2017, Zoltan Farkas All Rights Reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * Additionally licensed with:
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
package org.spf4j.io;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import org.spf4j.base.IntMath;
import org.spf4j.base.Strings;
import static org.spf4j.io.PipedOutputStreamTest.generateTestStr;
import static org.spf4j.io.PipedOutputStreamTest.test;
import org.spf4j.recyclable.impl.ArraySuppliers;

/**
 *
 * @author zoly
 */
public class MemorizingBufferedInputStreamTest {

    private static final String TSTR =
            "This is a super \u00EF cool, mega dupper test string for testing piping..........E";

    @Test
    @SuppressFBWarnings("SIC_INNER_SHOULD_BE_STATIC_ANON")
    public void testBuffering() throws IOException {
      byte [] bytes = new byte[16384];
      for (int i = 0; i< bytes.length; i++) {
        bytes[i] = (byte) ('a' + i % 10);
      }
      ByteArrayInputStream bis = new ByteArrayInputStream(bytes) {
        private boolean first = true;
        @Override
        public synchronized int read(byte[] b, int off, int len) {
          if (first && len > 260) {
            first = false;
            return super.read(b, off, 260);
          } else {
            return super.read(b, off, len);
          }
        }

      };
      try (MemorizingBufferedInputStream mbis = new MemorizingBufferedInputStream(bis)) {
        StringBuilder sb = new StringBuilder();
        PushbackInputStreamEx pushbackInputStreamEx = new PushbackInputStreamEx(mbis);
        int read = pushbackInputStreamEx.read();
        pushbackInputStreamEx.unread(read);
        CharStreams.copy(new InputStreamReader(pushbackInputStreamEx, StandardCharsets.UTF_8), sb);
        Assert.assertEquals(16384, sb.length());
        Assert.assertEquals(new String(bytes, StandardCharsets.US_ASCII), sb.toString());
      }
    }





    @Test
    public void testSimpleStreamBuffering() throws IOException {
        final byte[] array = Strings.toUtf8(TSTR);
        ByteArrayInputStream bis = new ByteArrayInputStream(array);
        MemorizingBufferedInputStream mis = new MemorizingBufferedInputStream(bis);
        int val;
        int i = 0;
        while ((val = mis.read()) > 0)  {
            System.out.print((char) val);
            Assert.assertEquals(array[i], (byte) val);
            i++;
        }
        Assert.assertEquals(i, array.length);
    }

    @Test
    public void testStreamCLose() throws IOException {
        final byte[] array = Strings.toUtf8(TSTR);
        ByteArrayInputStream bis = new ByteArrayInputStream(array);
        MemorizingBufferedInputStream mis = new MemorizingBufferedInputStream(bis, 8);

        for (int i = 0; i < 6; i++)  {
            int val = mis.read();
            System.out.print((char) val);
            Assert.assertEquals(array[i], (byte) val);
        }
        System.out.println(mis);
        mis.close();
        System.out.println(mis);
    }



    @Test
    public void testStreamBuffering() throws IOException {
        test(TSTR, 8, true);
        final IntMath.XorShift32 random = new IntMath.XorShift32();
        for (int i = 0; i < 100; i++) {
            int nrChars = Math.abs(random.nextInt() % 100000);
            StringBuilder sb = generateTestStr(nrChars);
            test(sb.toString(), Math.abs(random.nextInt() % 10000), true);
            testBuff(sb, 8192);
            testBuff(sb, 32);
        }
    }

    @SuppressFBWarnings("PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS")
    private void testBuff(final StringBuilder sb, final int buffSize) throws IOException {
        final byte[] utf8Bytes = Strings.toUtf8(sb.toString());
        ByteArrayInputStream bis = new ByteArrayInputStream(utf8Bytes);
        try (MemorizingBufferedInputStream mbis = new MemorizingBufferedInputStream(bis, buffSize, buffSize / 2,
                ArraySuppliers.Bytes.GL_SUPPLIER, Charsets.UTF_8)) {
            int val = mbis.read();
            Assert.assertEquals(val, mbis.getReadBytesFromBuffer()[0]);
            byte [] buff = new byte [8];
            int read = mbis.read(buff);
            Assert.assertEquals(8, read);
            Assert.assertTrue(Arrays.equals(buff, Arrays.copyOfRange(mbis.getReadBytesFromBuffer(), 1, 9)));
            int result;
            do {
                result = mbis.read(buff);
            } while (result >= 0);
            byte[] unredBytesEnd = mbis.getUnreadBytesFromBuffer();
            Assert.assertEquals(0, unredBytesEnd.length);
            final byte[] readBytesFromBuffer = mbis.getReadBytesFromBuffer();
            Assert.assertTrue(Arrays.equals(readBytesFromBuffer,
                    Arrays.copyOfRange(utf8Bytes, utf8Bytes.length - readBytesFromBuffer.length, utf8Bytes.length)));

        }
    }
}
