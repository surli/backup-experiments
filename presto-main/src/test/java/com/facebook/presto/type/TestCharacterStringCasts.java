/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.type;

import com.facebook.presto.operator.scalar.AbstractTestFunctions;
import org.testng.annotations.Test;

import java.util.stream.Stream;

import static com.facebook.presto.operator.scalar.CharacterStringCasts.varcharToCharSaturatedFloorCast;
import static com.facebook.presto.spi.type.CharType.createCharType;
import static com.facebook.presto.spi.type.VarcharType.VARCHAR;
import static com.facebook.presto.spi.type.VarcharType.createVarcharType;
import static io.airlift.slice.SliceUtf8.codePointToUtf8;
import static io.airlift.slice.Slices.utf8Slice;
import static io.airlift.slice.Slices.wrappedBuffer;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.testng.Assert.assertEquals;

public class TestCharacterStringCasts
        extends AbstractTestFunctions
{
    @Test
    public void testVarcharToVarcharCast()
            throws Exception
    {
        assertFunction("cast('bar' as varchar(20))", createVarcharType(20), "bar");
        assertFunction("cast(cast('bar' as varchar(20)) as varchar(30))", createVarcharType(30), "bar");
        assertFunction("cast(cast('bar' as varchar(20)) as varchar)", VARCHAR, "bar");

        assertFunction("cast('banana' as varchar(3))", createVarcharType(3), "ban");
        assertFunction("cast(cast('banana' as varchar(20)) as varchar(3))", createVarcharType(3), "ban");
    }

    @Test
    public void testVarcharToCharCast()
    {
        assertFunction("cast('bar  ' as char(10))", createCharType(10), "bar       ");
        assertFunction("cast('bar' as char)", createCharType(1), "b");
        assertFunction("cast('   ' as char)", createCharType(1), " ");
    }

    @Test
    public void testCharToVarcharCast()
            throws Exception
    {
        assertFunction("cast(cast('bar' as char(5)) as varchar(10))", createVarcharType(10), "bar  ");
        assertFunction("cast(cast('bar' as char(5)) as varchar(1))", createVarcharType(1), "b");
        assertFunction("cast(cast('b' as char(5)) as varchar(2))", createVarcharType(2), "b ");
        assertFunction("cast(cast('b' as char(5)) as varchar(1))", createVarcharType(1), "b");
        assertFunction("cast(cast('bar' as char(3)) as varchar(3))", createVarcharType(3), "bar");
        assertFunction("cast(cast('b' as char(3)) as varchar(3))", createVarcharType(3), "b  ");
    }

    @Test
    public void testVarcharToCharSaturatedFloorCast()
    {
        // Encoded in UTF-8
        byte[] nonBmpCharacter = codePointToUtf8(0x1F50D).getBytes();
        byte[] nonBmpCharacterMinus1 = codePointToUtf8(0x1F50C).getBytes();
        byte[] maxCodePoint = codePointToUtf8(Character.MAX_CODE_POINT).getBytes();
        byte[] codePointBeforeSpace = codePointToUtf8(' ' - 1).getBytes();

        // Truncation
        assertEquals(varcharToCharSaturatedFloorCast(
                4L,
                utf8Slice("12345")),
                utf8Slice("1234"));

        // Size fits, preserved
        assertEquals(varcharToCharSaturatedFloorCast(
                4L,
                utf8Slice("1234")),
                utf8Slice("1234"));
        assertEquals(varcharToCharSaturatedFloorCast(
                4L,
                wrappedBuffer(concat(bytes("123"), nonBmpCharacter))),
                wrappedBuffer(concat(bytes("123"), nonBmpCharacter)));
        assertEquals(varcharToCharSaturatedFloorCast(
                4L,
                wrappedBuffer(concat(bytes("12"), nonBmpCharacter, bytes("3")))),
                wrappedBuffer(concat(bytes("12"), nonBmpCharacter, bytes("3"))));

        // Size fits, preserved except char(4) representation has trailing spaces removed
        assertEquals(varcharToCharSaturatedFloorCast(
                4L,
                utf8Slice("123 ")),
                utf8Slice("123"));

        // Too short, casted back would be padded with ' ' and thus made greater (VarcharOperators.lessThan), so last character needs decrementing
        assertEquals(varcharToCharSaturatedFloorCast(
                4L,
                utf8Slice("123")),
                wrappedBuffer(concat(bytes("122"), maxCodePoint)));
        assertEquals(varcharToCharSaturatedFloorCast(
                4L,
                utf8Slice("12 ")),
                wrappedBuffer(concat(bytes("12"), codePointBeforeSpace, maxCodePoint)));
        assertEquals(varcharToCharSaturatedFloorCast(
                4L,
                utf8Slice("1  ")),
                wrappedBuffer(concat(bytes("1 "), codePointBeforeSpace, maxCodePoint)));
        assertEquals(varcharToCharSaturatedFloorCast(
                4L,
                utf8Slice(" ")),
                wrappedBuffer(concat(codePointBeforeSpace, maxCodePoint, maxCodePoint, maxCodePoint)));
        assertEquals(varcharToCharSaturatedFloorCast(
                4L,
                wrappedBuffer(concat(bytes("12"), nonBmpCharacter))),
                wrappedBuffer(concat(bytes("12"), nonBmpCharacterMinus1, maxCodePoint)));
        assertEquals(varcharToCharSaturatedFloorCast(
                4L,
                wrappedBuffer(concat(bytes("1"), nonBmpCharacter, bytes("3")))),
                wrappedBuffer(concat(bytes("1"), nonBmpCharacter, bytes("2"), maxCodePoint)));

        // Too short, casted back would be padded with ' ' and thus made greater (VarcharOperators.lessThan), previous to last needs decrementing since last is \0
        assertEquals(varcharToCharSaturatedFloorCast(
                4L,
                utf8Slice("12\0")),
                wrappedBuffer(concat(bytes("11"), maxCodePoint, maxCodePoint)));
        assertEquals(varcharToCharSaturatedFloorCast(
                4L,
                utf8Slice("1\0")),
                wrappedBuffer(concat(bytes("0"), maxCodePoint, maxCodePoint, maxCodePoint)));

        // Smaller than any char(4) casted back to varchar, so the result is lowest char(4) possible
        assertEquals(varcharToCharSaturatedFloorCast(
                4L,
                utf8Slice("\0")),
                utf8Slice("\0\0\0\0"));
        assertEquals(varcharToCharSaturatedFloorCast(
                4L,
                utf8Slice("\0\0")),
                utf8Slice("\0\0\0\0"));
        assertEquals(varcharToCharSaturatedFloorCast(
                4L,
                utf8Slice("")),
                utf8Slice("\0\0\0\0"));
    }

    private byte[] bytes(String stringWithinBmp)
    {
        return stringWithinBmp.getBytes(UTF_8);
    }

    private byte[] concat(byte[]... buffers)
    {
        int resultLength = Stream.of(buffers)
                .mapToInt(buffer -> buffer.length)
                .sum();
        byte[] result = new byte[resultLength];
        int offset = 0;
        for (byte[] buffer : buffers) {
            System.arraycopy(buffer, 0, result, offset, buffer.length);
            offset += buffer.length;
        }
        return result;
    }
}
