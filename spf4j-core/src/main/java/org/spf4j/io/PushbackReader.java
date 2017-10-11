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

import edu.umd.cs.findbugs.annotations.CleanupObligation;
import edu.umd.cs.findbugs.annotations.DischargesObligation;
import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

@CleanupObligation
public final class PushbackReader extends FilterReader {

    private char[] buf;

    private int pos;

    public PushbackReader(final Reader in, final int size) {
        super(in);
        if (size <= 0) {
            throw new IllegalArgumentException("size " + size + " <= 0");
        }
        this.buf = new char[size];
        this.pos = size;
    }

    public PushbackReader(final Reader in) {
        this(in, 1);
    }

    private void ensureOpen() throws IOException {
        if (buf == null) {
            throw new IOException("Stream closed " + this);
        }
    }

    @Override
    public int read() throws IOException {
        ensureOpen();
        if (pos < buf.length) {
            return buf[pos++];
        } else {
            return super.read();
        }
    }

    @Override
    public int read(final char[] cbuf, final int poff, final int plen) throws IOException {
        int off = poff;
        int len = plen;
        ensureOpen();
        if (len <= 0) {
            if (len < 0) {
                throw new IndexOutOfBoundsException("len = " + len);
            } else if ((off < 0) || (off > cbuf.length)) {
                throw new IndexOutOfBoundsException("off = " + off);
            }
            return 0;
        }
        int avail = buf.length - pos;
        if (avail > 0) {
            if (len < avail) {
                avail = len;
            }
            System.arraycopy(buf, pos, cbuf, off, avail);
            pos += avail;
            off += avail;
            len -= avail;
        }
        if (len > 0) {
            len = super.read(cbuf, off, len);
            if (len == -1) {
                return (avail == 0) ? -1 : avail;
            }
            return avail + len;
        }
        return avail;
    }

    public void unread(final int c) throws IOException {
        ensureOpen();
        if (pos == 0) {
            throw new IOException("Pushback buffer overflow " + this);
        }
        if (c >= 0) {
          buf[--pos] = (char) c;
        } else {
          throw new IllegalArgumentException("pushing back invalid character " + c);
        }
    }

    public void unread(final char[] cbuf, final int off, final int len) throws IOException {
        ensureOpen();
        if (len > pos) {
            throw new IOException("Pushback buffer overflow " + this);
        }
        pos -= len;
        System.arraycopy(cbuf, off, buf, pos, len);
    }

    public void unread(final char[] cbuf) throws IOException {
        unread(cbuf, 0, cbuf.length);
    }

    @Override
    public boolean ready() throws IOException {
        ensureOpen();
        return (pos < buf.length) || super.ready();
    }

    @Override
    public void mark(final int readAheadLimit) throws IOException {
        throw new IOException("mark/reset not supported " + this);
    }

    @Override
    public void reset() throws IOException {
        throw new IOException("mark/reset not supported " + this);
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    @DischargesObligation
    public void close() throws IOException {
        super.close();
        buf = null;
    }

    @Override
    public long skip(final long pn) throws IOException {
        long n = pn;
        if (n < 0L) {
            throw new IllegalArgumentException("skip value is negative " + pn);
        }
        ensureOpen();
        int avail = buf.length - pos;
        if (avail > 0) {
            if (n <= avail) {
                pos += n;
                return n;
            } else {
                pos = buf.length;
                n -= avail;
            }
        }
        return avail + super.skip(n);
    }

    @Override
    public String toString() {
        return "PushbackReader{" + "buf=" + new String(buf)
                + ", pos=" + pos + ", wrapped=" + super.in + '}';
    }



}
