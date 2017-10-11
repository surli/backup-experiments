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
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;

/**
 * {@link OutputStream} implementation that transforms a byte stream to a
 * character stream using a specified character set encoding and writes the resulting
 * stream to a {@link Writer}. The stream is transformed using a
 * {@link CharsetDecoder} object, guaranteeing that all character set
 * encodings supported by the JRE are handled correctly.
 * <p>
 * The output of the {@link CharsetDecoder} is buffered using a fixed size buffer.
 * This implies that the data is written to the underlying {@link Writer} in chunks
 * that are no larger than the size of this buffer. By default, the buffer is
 * flushed only when it overflows or when {@link #flush()} or {@link #close()}
 * is called. In general there is therefore no need to wrap the underlying {@link Writer}
 * in a {@link java.io.BufferedWriter}. {@link WriterOutputStream} can also
 * be instructed to flush the buffer after each write operation. In this case, all
 * available data is written immediately to the underlying {@link Writer}, implying that
 * the current position of the {@link Writer} is correlated to the current position
 * of the {@link WriterOutputStream}.
 * <p>
 * {@link WriterOutputStream} implements the inverse transformation of {@link java.io.OutputStreamWriter};
 * in the following example, writing to {@code out2} would have the same result as writing to
 * {@code out} directly (provided that the byte sequence is legal with respect to the
 * character set encoding):
 * <pre>
 * OutputStream out = ...
 * Charset cs = ...
 * OutputStreamWriter writer = new OutputStreamWriter(out, cs);
 * WriterOutputStream out2 = new WriterOutputStream(writer, cs);</pre>
 * {@link WriterOutputStream} implements the same transformation as {@link java.io.InputStreamReader},
 * except that the control flow is reversed: both classes transform a byte stream
 * into a character stream, but {@link java.io.InputStreamReader} pulls data from the underlying stream,
 * while {@link WriterOutputStream} pushes it to the underlying stream.
 * <p>
 * Note that while there are use cases where there is no alternative to using
 * this class, very often the need to use this class is an indication of a flaw
 * in the design of the code. This class is typically used in situations where an existing
 * API only accepts an {@link OutputStream} object, but where the stream is known to represent
 * character data that must be decoded for further use.
 * <p>
 * Instances of {@link WriterOutputStream} are not thread safe.
 * 
 * @see org.apache.commons.io.input.ReaderInputStream
 * 
 * @since 7.2.25
 */
@CleanupObligation
public final class WriterOutputStream extends AppendableOutputStream {
    private static final int DEFAULT_BUFFER_SIZE = 8192;

    private final Writer writer;


    /**
     * Constructs a new {@link WriterOutputStream} with a default output buffer size of
     * 1024 characters. The output buffer will only be flushed when it overflows or when
     * {@link #flush()} or {@link #close()} is called.
     * 
     * @param writer the target {@link Writer}
     * @param decoder the charset decoder
     * @since 2.1
     */
    public WriterOutputStream(final Writer writer, final CharsetDecoder decoder) {
        this(writer, decoder, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Constructs a new {@link WriterOutputStream}.
     * 
     * @param writer the target {@link Writer}
     * @param decoder the charset decoder
     * @param bufferSize the size of the output buffer in number of characters
     * @since 2.1
     */
    public WriterOutputStream(final Writer writer, final CharsetDecoder decoder, final int bufferSize) {
        super(writer, decoder, bufferSize);
        this.writer = writer;
    }

    /**
     * Constructs a new {@link WriterOutputStream}.
     * 
     * @param writer the target {@link Writer}
     * @param charset the charset encoding
     * @param bufferSize the size of the output buffer in number of characters
     */
    public WriterOutputStream(final Writer writer, final Charset charset, final int bufferSize) {
        this(writer,
             charset.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPLACE)
                    .onUnmappableCharacter(CodingErrorAction.REPLACE)
                    .replaceWith("?"),
             bufferSize);
    }

    /**
     * Constructs a new {@link WriterOutputStream} with a default output buffer size of
     * 1024 characters. The output buffer will only be flushed when it overflows or when
     * {@link #flush()} or {@link #close()} is called.
     * 
     * @param writer the target {@link Writer}
     * @param charset the charset encoding
     */
    public WriterOutputStream(final Writer writer, final Charset charset) {
        this(writer, charset, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Constructs a new {@link WriterOutputStream}.
     * 
     * @param writer the target {@link Writer}
     * @param charsetName the name of the charset encoding
     * @param bufferSize the size of the output buffer in number of characters
     */
    public WriterOutputStream(final Writer writer, final String charsetName, final int bufferSize) {
        this(writer, Charset.forName(charsetName), bufferSize);
    }

    /**
     * Constructs a new {@link WriterOutputStream} with a default output buffer size of
     * 1024 characters. The output buffer will only be flushed when it overflows or when
     * {@link #flush()} or {@link #close()} is called.
     * 
     * @param writer the target {@link Writer}
     * @param charsetName the name of the charset encoding
     */
    public WriterOutputStream(final Writer writer, final String charsetName) {
        this(writer, charsetName, DEFAULT_BUFFER_SIZE);
    }



    /**
     * Flush the stream. Any remaining content accumulated in the output buffer
     * will be written to the underlying {@link Writer}. After that
     * {@link Writer#flush()} will be called.
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void flush() throws IOException {
        super.flush();
        writer.flush();
    }

    /**
     * Close the stream. Any remaining content accumulated in the output buffer
     * will be written to the underlying {@link Writer}. After that
     * {@link Writer#close()} will be called.
     * @throws IOException if an I/O error occurs
     */
    @Override
    @DischargesObligation
    public void close() throws IOException {
        super.close();
        writer.close();
    }


    /**
     * Flush the output.
     * 
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void flushOutput() throws IOException {
        if (decoderOut.position() > 0) {
            writer.write(decoderOut.array(), 0, decoderOut.position());
            decoderOut.rewind();
        }
    }

    @Override
    public String toString() {
        return "WriterOutputStream{" + "writer=" + writer + '}';
    }
    
}
