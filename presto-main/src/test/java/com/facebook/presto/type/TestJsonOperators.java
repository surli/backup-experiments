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

import static com.facebook.presto.spi.StandardErrorCode.INVALID_CAST_ARGUMENT;
import static com.facebook.presto.spi.type.BigintType.BIGINT;
import static com.facebook.presto.spi.type.BooleanType.BOOLEAN;
import static com.facebook.presto.spi.type.DecimalType.createDecimalType;
import static com.facebook.presto.spi.type.DoubleType.DOUBLE;
import static com.facebook.presto.spi.type.VarcharType.VARCHAR;
import static com.facebook.presto.type.JsonType.JSON;
import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.POSITIVE_INFINITY;

public class TestJsonOperators
        extends AbstractTestFunctions
{
    // Some of the tests in this class are expected to fail when coercion between primitive presto types changes behavior

    @Test
    public void testCastToBigint()
    {
        assertFunction("cast(JSON 'null' as BIGINT)", BIGINT, null);
        assertFunction("cast(JSON '128' as BIGINT)", BIGINT, 128L);
        assertInvalidFunction("cast(JSON '12345678901234567890' as BIGINT)", INVALID_CAST_ARGUMENT);
        assertFunction("cast(JSON '128.9' as BIGINT)", BIGINT, 129L);
        assertFunction("cast(JSON '1234567890123456789.0' as BIGINT)", BIGINT, 1234567890123456768L); // loss of precision
        assertFunction("cast(JSON '12345678901234567890.0' as BIGINT)", BIGINT, 9223372036854775807L); // overflow. unexpected behavior. coherent with rest of Presto.
        assertFunction("cast(JSON '1e-324' as BIGINT)", BIGINT, 0L);
        assertInvalidFunction("cast(JSON '1e309' as BIGINT)", INVALID_CAST_ARGUMENT);
        assertFunction("cast(JSON 'true' as BIGINT)", BIGINT, 1L);
        assertFunction("cast(JSON 'false' as BIGINT)", BIGINT, 0L);
        assertFunction("cast(JSON '\"128\"' as BIGINT)", BIGINT, 128L);
        assertInvalidFunction("cast(JSON '\"12345678901234567890\"' as BIGINT)", INVALID_CAST_ARGUMENT);
        assertInvalidFunction("cast(JSON '\"128.9\"' as BIGINT)", INVALID_CAST_ARGUMENT);
        assertInvalidFunction("cast(JSON '\"true\"' as BIGINT)", INVALID_CAST_ARGUMENT);
        assertInvalidFunction("cast(JSON '\"false\"' as BIGINT)", INVALID_CAST_ARGUMENT);

        assertFunction("cast(JSON ' 128' as BIGINT)", BIGINT, 128L); // leading space

        assertFunction("cast(json_extract('{\"x\":999}', '$.x') as BIGINT)", BIGINT, 999L);
        assertInvalidCast("cast(JSON '{ \"x\" : 123}' as BIGINT)");
    }

    @Test
    public void testTypeConstructor()
            throws Exception
    {
        assertFunction("JSON '123'", JSON, "123");
        assertFunction("JSON '[4,5,6]'", JSON, "[4,5,6]");
        assertFunction("JSON '{ \"a\": 789 }'", JSON, "{\"a\":789}");
    }

    @Test
    public void testCastFromIntegrals()
    {
        assertFunction("cast(cast (null as integer) as JSON)", JSON, null);
        assertFunction("cast(cast (null as bigint) as JSON)", JSON, null);
        assertFunction("cast(128 as JSON)", JSON, "128");
        assertFunction("cast(BIGINT '128' as JSON)", JSON, "128");
    }

    @Test
    public void testCastToDouble()
            throws Exception
    {
        assertFunction("cast(JSON 'null' as DOUBLE)", DOUBLE, null);
        assertFunction("cast(JSON '128' as DOUBLE)", DOUBLE, 128.0);
        assertFunction("cast(JSON '12345678901234567890' as DOUBLE)", DOUBLE, 1.2345678901234567e19);
        assertFunction("cast(JSON '128.9' as DOUBLE)", DOUBLE, 128.9);
        assertFunction("cast(JSON '1e-324' as DOUBLE)", DOUBLE, 0.0); // smaller than minimum subnormal positive
        assertFunction("cast(JSON '1e309' as DOUBLE)", DOUBLE, POSITIVE_INFINITY); // overflow
        assertFunction("cast(JSON '-1e309' as DOUBLE)", DOUBLE, NEGATIVE_INFINITY); // underflow
        assertFunction("cast(JSON 'true' as DOUBLE)", DOUBLE, 1.0);
        assertFunction("cast(JSON 'false' as DOUBLE)", DOUBLE, 0.0);
        assertFunction("cast(JSON '\"128\"' as DOUBLE)", DOUBLE, 128.0);
        assertFunction("cast(JSON '\"12345678901234567890\"' as DOUBLE)", DOUBLE, 1.2345678901234567e19);
        assertFunction("cast(JSON '\"128.9\"' as DOUBLE)", DOUBLE, 128.9);
        assertFunction("cast(JSON '\"NaN\"' as DOUBLE)", DOUBLE, Double.NaN);
        assertFunction("cast(JSON '\"Infinity\"' as DOUBLE)", DOUBLE, POSITIVE_INFINITY);
        assertFunction("cast(JSON '\"-Infinity\"' as DOUBLE)", DOUBLE, NEGATIVE_INFINITY);
        assertInvalidFunction("cast(JSON '\"true\"' as DOUBLE)", INVALID_CAST_ARGUMENT);

        assertFunction("cast(JSON ' 128.9' as DOUBLE)", DOUBLE, 128.9); // leading space

        assertFunction("cast(json_extract('{\"x\":1.23}', '$.x') as DOUBLE)", DOUBLE, 1.23);
        assertInvalidCast("cast(JSON '{ \"x\" : 123}' as DOUBLE)");
    }

    @Test
    public void testCastFromDouble()
            throws Exception
    {
        assertFunction("cast(cast (null as double) as JSON)", JSON, null);
        assertFunction("cast(3.14 as JSON)", JSON, "3.14");
        assertFunction("cast(nan() as JSON)", JSON, "\"NaN\"");
        assertFunction("cast(infinity() as JSON)", JSON, "\"Infinity\"");
        assertFunction("cast(-infinity() as JSON)", JSON, "\"-Infinity\"");
    }

    @Test
    public void testCastToDecimal()
            throws Exception
    {
        assertFunction("cast(JSON 'null' as DECIMAL(10,3))", createDecimalType(10, 3), null);
        assertFunction("cast(JSON '128' as DECIMAL(10,3))", createDecimalType(10, 3), decimal("128.000"));
        assertFunction("cast(cast(DECIMAL '123456789012345678901234567890.12345678' as JSON) as DECIMAL(38,8))", createDecimalType(38, 8), decimal("123456789012345678901234567890.12345678"));
        assertFunction("cast(JSON '123.456' as DECIMAL(10,5))", createDecimalType(10, 5), decimal("123.45600"));
        assertFunction("cast(JSON 'true' as DECIMAL(10,5))", createDecimalType(10, 5), decimal("1.00000"));
        assertFunction("cast(JSON 'false' as DECIMAL(10,5))", createDecimalType(10, 5), decimal("0.00000"));
        assertInvalidCast("cast(JSON '1234567890123456' as DECIMAL(10,3))", "Cannot cast input json to DECIMAL(10,3)");
        assertInvalidCast("cast(JSON '{ \"x\" : 123}' as DECIMAL(10,3))", "Cannot cast '{\"x\":123}' to DECIMAL(10,3)");
        assertInvalidCast("cast(JSON '\"abc\"' as DECIMAL(10,3))", "Cannot cast '\"abc\"' to DECIMAL(10,3)");
    }

    @Test
    public void testCastFromDecimal()
            throws Exception
    {
        assertFunction("cast(cast(null as decimal(5,2)) as JSON)", JSON, null);
        assertFunction("cast(3.14 as JSON)", JSON, "3.14");
        assertFunction("cast(DECIMAL '12345678901234567890.123456789012345678' as JSON)", JSON, "12345678901234567890.123456789012345678");
    }

    @Test
    public void testCastToBoolean()
    {
        assertFunction("cast(JSON 'null' as BOOLEAN)", BOOLEAN, null);
        assertFunction("cast(JSON '0' as BOOLEAN)", BOOLEAN, false);
        assertFunction("cast(JSON '128' as BOOLEAN)", BOOLEAN, true);
        assertInvalidFunction("cast(JSON '12345678901234567890' as BOOLEAN)", INVALID_CAST_ARGUMENT);
        assertFunction("cast(JSON '128.9' as BOOLEAN)", BOOLEAN, true);
        assertFunction("cast(JSON '1e-324' as BOOLEAN)", BOOLEAN, false); // smaller than minimum subnormal positive
        assertInvalidFunction("cast(JSON '1e309' as BOOLEAN)", INVALID_CAST_ARGUMENT); // overflow
        assertFunction("cast(JSON 'true' as BOOLEAN)", BOOLEAN, true);
        assertFunction("cast(JSON 'false' as BOOLEAN)", BOOLEAN, false);
        assertFunction("cast(JSON '\"True\"' as BOOLEAN)", BOOLEAN, true);
        assertFunction("cast(JSON '\"true\"' as BOOLEAN)", BOOLEAN, true);
        assertFunction("cast(JSON '\"false\"' as BOOLEAN)", BOOLEAN, false);
        assertInvalidFunction("cast(JSON '\"128\"' as BOOLEAN)", INVALID_CAST_ARGUMENT);
        assertInvalidFunction("cast(JSON '\"\"' as BOOLEAN)", INVALID_CAST_ARGUMENT);

        assertFunction("cast(JSON ' true' as BOOLEAN)", BOOLEAN, true); // leading space

        assertFunction("cast(json_extract('{\"x\":true}', '$.x') as BOOLEAN)", BOOLEAN, true);
        assertInvalidCast("cast(JSON '{ \"x\" : 123}' as BOOLEAN)");
    }

    @Test
    public void testCastFromBoolean()
    {
        assertFunction("cast(cast (null as boolean) as JSON)", JSON, null);
        assertFunction("cast(TRUE as JSON)", JSON, "true");
        assertFunction("cast(FALSE as JSON)", JSON, "false");
    }

    @Test
    public void testCastToVarchar()
    {
        assertFunction("cast(JSON 'null' as VARCHAR)", VARCHAR, null);
        assertFunction("cast(JSON '128' as VARCHAR)", VARCHAR, "128");
        assertFunction("cast(JSON '12345678901234567890' as VARCHAR)", VARCHAR, "12345678901234567890"); // overflow, no loss of precision
        assertFunction("cast(JSON '128.9' as VARCHAR)", VARCHAR, "128.9");
        assertFunction("cast(JSON '1e-324' as VARCHAR)", VARCHAR, "0.0"); // smaller than minimum subnormal positive
        assertFunction("cast(JSON '1e309' as VARCHAR)", VARCHAR, "Infinity"); // overflow
        assertFunction("cast(JSON '-1e309' as VARCHAR)", VARCHAR, "-Infinity"); // underflow
        assertFunction("cast(JSON 'true' as VARCHAR)", VARCHAR, "true");
        assertFunction("cast(JSON 'false' as VARCHAR)", VARCHAR, "false");
        assertFunction("cast(JSON '\"test\"' as VARCHAR)", VARCHAR, "test");
        assertFunction("cast(JSON '\"null\"' as VARCHAR)", VARCHAR, "null");
        assertFunction("cast(JSON '\"\"' as VARCHAR)", VARCHAR, "");

        assertFunction("cast(JSON ' \"test\"' as VARCHAR)", VARCHAR, "test"); // leading space

        assertFunction("cast(json_extract('{\"x\":\"y\"}', '$.x') as VARCHAR)", VARCHAR, "y");
        assertInvalidCast("cast(JSON '{ \"x\" : 123}' as VARCHAR)");
    }

    @Test
    public void testCastFromVarchar()
    {
        assertFunction("cast(cast (null as varchar) as JSON)", JSON, null);
        assertFunction("cast('abc' as JSON)", JSON, "\"abc\"");
        assertFunction("cast('\"a\":2' as JSON)", JSON, "\"\\\"a\\\":2\"");
    }

    @Test
    public void testCastFromArray()
    {
        assertFunction("cast(cast (null as ARRAY<BIGINT>) AS JSON)", JSON, null);
        assertFunction("cast(ARRAY[] AS JSON)", JSON, "[]");
        assertFunction("cast(ARRAY[null, null] AS JSON)", JSON, "[null,null]");

        assertFunction("cast(ARRAY[true, false, null] AS JSON)", JSON, "[true,false,null]");

        assertFunction("cast(cast(ARRAY[1, 2, null] AS ARRAY<TINYINT>) AS JSON)", JSON, "[1,2,null]");
        assertFunction("cast(cast(ARRAY[12345, -12345, null] AS ARRAY<SMALLINT>) AS JSON)", JSON, "[12345,-12345,null]");
        assertFunction("cast(cast(ARRAY[123456789, -123456789, null] AS ARRAY<INTEGER>) AS JSON)", JSON, "[123456789,-123456789,null]");
        assertFunction("cast(cast(ARRAY[1234567890123456789, -1234567890123456789, null] AS ARRAY<BIGINT>) AS JSON)", JSON, "[1234567890123456789,-1234567890123456789,null]");

        assertFunction("CAST(CAST(ARRAY[3.14, nan(), infinity(), -infinity(), null] AS ARRAY<REAL>) AS JSON)", JSON, "[3.14,\"NaN\",\"Infinity\",\"-Infinity\",null]");
        assertFunction("CAST(ARRAY[3.14, 1e-323, 1e308, nan(), infinity(), -infinity(), null] AS JSON)", JSON, "[3.14,1.0E-323,1.0E308,\"NaN\",\"Infinity\",\"-Infinity\",null]");
        assertFunction("CAST(ARRAY[DECIMAL '3.14', null] AS JSON)", JSON, "[3.14,null]");
        assertFunction("CAST(ARRAY[DECIMAL '12345678901234567890.123456789012345678', null] AS JSON)", JSON, "[12345678901234567890.123456789012345678,null]");

        assertFunction("cast(ARRAY['a', 'bb', null] AS JSON)", JSON, "[\"a\",\"bb\",null]");
        assertFunction(
                "cast(ARRAY[JSON '123', JSON '3.14', JSON 'false', JSON '\"abc\"', JSON '[1, \"a\", null]', JSON '{\"a\": 1, \"b\": \"str\", \"c\": null}', JSON 'null', null] AS JSON)",
                JSON,
                "[123,3.14,false,\"abc\",[1,\"a\",null],{\"a\":1,\"b\":\"str\",\"c\":null},null,null]");

        assertFunction(
                "cast(ARRAY[ARRAY[1, 2], ARRAY[3, null], ARRAY[], ARRAY[null, null], null] AS JSON)",
                JSON,
                "[[1,2],[3,null],[],[null,null],null]");
        assertFunction(
                "cast(ARRAY[MAP(ARRAY['a', 'b'], ARRAY[1, 2]), MAP(ARRAY['three', 'none'], ARRAY[3, null]), MAP(), MAP(ARRAY['h1', 'h2'], ARRAY[null, null]), null] AS JSON)",
                JSON,
                "[{\"a\":1,\"b\":2},{\"three\":3,\"none\":null},{},{\"h1\":null,\"h2\":null},null]");
        assertFunction(
                "cast(ARRAY[ROW(1, 2), ROW(3, CAST(null as INTEGER)), CAST(ROW(null, null) AS ROW(INTEGER, INTEGER)), null] AS JSON)",
                JSON,
                "[[1,2],[3,null],[null,null],null]");
    }

    @Test
    public void testCastFromMap()
    {
        assertFunction("cast(cast (null as MAP<BIGINT, BIGINT>) AS JSON)", JSON, null);
        assertFunction("cast(MAP() AS JSON)", JSON, "{}");
        assertFunction("cast(MAP(ARRAY[1, 2], ARRAY[null, null]) AS JSON)", JSON, "{\"1\":null,\"2\":null}");

        // Test key types
        assertFunction("CAST(MAP(ARRAY[true, false], ARRAY[1, 2]) AS JSON)", JSON, "{\"false\":2,\"true\":1}");

        assertFunction(
                "cast(MAP(cast(ARRAY[1, 2, 3] AS ARRAY<TINYINT>), ARRAY[5, 8, null]) AS JSON)",
                JSON,
                "{\"1\":5,\"2\":8,\"3\":null}");
        assertFunction(
                "cast(MAP(cast(ARRAY[12345, 12346, 12347] AS ARRAY<SMALLINT>), ARRAY[5, 8, null]) AS JSON)",
                JSON,
                "{\"12345\":5,\"12346\":8,\"12347\":null}");
        assertFunction(
                "cast(MAP(cast(ARRAY[123456789,123456790,123456791] AS ARRAY<INTEGER>), ARRAY[5, 8, null]) AS JSON)",
                JSON,
                "{\"123456789\":5,\"123456790\":8,\"123456791\":null}");
        assertFunction(
                "cast(MAP(cast(ARRAY[1234567890123456111,1234567890123456222,1234567890123456777] AS ARRAY<BIGINT>), ARRAY[111, 222, null]) AS JSON)",
                JSON,
                "{\"1234567890123456111\":111,\"1234567890123456222\":222,\"1234567890123456777\":null}");

        assertFunction(
                "cast(MAP(cast(ARRAY[3.14, 1e10, 1e20] AS ARRAY<REAL>), ARRAY[null, 10, 20]) AS JSON)",
                JSON,
                "{\"1.0E10\":10,\"1.0E20\":20,\"3.14\":null}");

        assertFunction(
                "cast(MAP(ARRAY[1e-323,1e308,nan()], ARRAY[-323,308,null]) AS JSON)",
                JSON,
                "{\"1.0E-323\":-323,\"1.0E308\":308,\"NaN\":null}");
        assertFunction(
                "cast(MAP(ARRAY[DECIMAL '3.14', DECIMAL '0.01'], ARRAY[0.14, null]) AS JSON)",
                JSON,
                "{\"0.01\":null,\"3.14\":0.14}");

        assertFunction(
                "cast(MAP(ARRAY[DECIMAL '12345678901234567890.1234567890666666', DECIMAL '0.0'], ARRAY[666666, null]) AS JSON)",
                JSON,
                "{\"0E-16\":null,\"12345678901234567890.1234567890666666\":666666}");

        assertFunction("CAST(MAP(ARRAY['a', 'bb', 'ccc'], ARRAY[1, 2, 3]) AS JSON)", JSON, "{\"a\":1,\"bb\":2,\"ccc\":3}");

        // Test value types
        assertFunction("cast(MAP(ARRAY[1, 2, 3], ARRAY[true, false, null]) AS JSON)", JSON, "{\"1\":true,\"2\":false,\"3\":null}");

        assertFunction(
                "cast(MAP(ARRAY[1, 2, 3], cast(ARRAY[5, 8, null] AS ARRAY<TINYINT>)) AS JSON)",
                JSON,
                "{\"1\":5,\"2\":8,\"3\":null}");
        assertFunction(
                "cast(MAP(ARRAY[1, 2, 3], cast(ARRAY[12345, -12345, null] AS ARRAY<SMALLINT>)) AS JSON)",
                JSON,
                "{\"1\":12345,\"2\":-12345,\"3\":null}");
        assertFunction(
                "cast(MAP(ARRAY[1, 2, 3], cast(ARRAY[123456789, -123456789, null] AS ARRAY<INTEGER>)) AS JSON)",
                JSON,
                "{\"1\":123456789,\"2\":-123456789,\"3\":null}");
        assertFunction(
                "cast(MAP(ARRAY[1, 2, 3], cast(ARRAY[1234567890123456789, -1234567890123456789, null] AS ARRAY<BIGINT>)) AS JSON)",
                JSON,
                "{\"1\":1234567890123456789,\"2\":-1234567890123456789,\"3\":null}");

        assertFunction(
                "CAST(MAP(ARRAY[1, 2, 3, 5, 8], CAST(ARRAY[3.14, nan(), infinity(), -infinity(), null] AS ARRAY<REAL>)) AS JSON)",
                JSON,
                "{\"1\":3.14,\"2\":\"NaN\",\"3\":\"Infinity\",\"5\":\"-Infinity\",\"8\":null}");
        assertFunction(
                "CAST(MAP(ARRAY[1, 2, 3, 5, 8, 13, 21], ARRAY[3.14, 1e-323, 1e308, nan(), infinity(), -infinity(), null]) AS JSON)",
                JSON,
                "{\"1\":3.14,\"13\":\"-Infinity\",\"2\":1.0E-323,\"21\":null,\"3\":1.0E308,\"5\":\"NaN\",\"8\":\"Infinity\"}");
        assertFunction("CAST(MAP(ARRAY[1, 2], ARRAY[DECIMAL '3.14', null]) AS JSON)", JSON, "{\"1\":3.14,\"2\":null}");
        assertFunction(
                "CAST(MAP(ARRAY[1, 2], ARRAY[DECIMAL '12345678901234567890.123456789012345678', null]) AS JSON)",
                JSON,
                "{\"1\":12345678901234567890.123456789012345678,\"2\":null}");

        assertFunction("cast(MAP(ARRAY[1, 2, 3], ARRAY['a', 'bb', null]) AS JSON)", JSON, "{\"1\":\"a\",\"2\":\"bb\",\"3\":null}");
        assertFunction(
                "CAST(MAP(ARRAY[1, 2, 3, 5, 8, 13, 21, 34], ARRAY[JSON '123', JSON '3.14', JSON 'false', JSON '\"abc\"', JSON '[1, \"a\", null]', JSON '{\"a\": 1, \"b\": \"str\", \"c\": null}', JSON 'null', null]) AS JSON)",
                JSON,
                "{\"1\":123,\"13\":{\"a\":1,\"b\":\"str\",\"c\":null},\"2\":3.14,\"21\":null,\"3\":false,\"34\":null,\"5\":\"abc\",\"8\":[1,\"a\",null]}");

        assertFunction(
                "cast(MAP(ARRAY[1, 2, 3, 5, 8], ARRAY[ARRAY[1, 2], ARRAY[3, null], ARRAY[], ARRAY[null, null], null]) AS JSON)",
                JSON,
                "{\"1\":[1,2],\"2\":[3,null],\"3\":[],\"5\":[null,null],\"8\":null}");
        assertFunction(
                "cast(MAP(ARRAY[1, 2, 3, 5, 8], ARRAY[MAP(ARRAY['a', 'b'], ARRAY[1, 2]), MAP(ARRAY['three', 'none'], ARRAY[3, null]), MAP(), MAP(ARRAY['h1', 'h2'], ARRAY[null, null]), null]) AS JSON)",
                JSON,
                "{\"1\":{\"a\":1,\"b\":2},\"2\":{\"none\":null,\"three\":3},\"3\":{},\"5\":{\"h1\":null,\"h2\":null},\"8\":null}");
        assertFunction(
                "cast(MAP(ARRAY[1, 2, 3, 5], ARRAY[ROW(1, 2), ROW(3, CAST(null as INTEGER)), CAST(ROW(null, null) AS ROW(INTEGER, INTEGER)), null]) AS JSON)",
                JSON,
                "{\"1\":[1,2],\"2\":[3,null],\"3\":[null,null],\"5\":null}");
    }

    @Test
    public void testCastFromRow()
    {
        assertFunction("cast(cast (null as ROW(BIGINT, VARCHAR)) AS JSON)", JSON, null);
        assertFunction("cast(ROW(null, null) as json)", JSON, "[null,null]");

        assertFunction("cast(ROW(true, false, null) AS JSON)", JSON, "[true,false,null]");

        assertFunction(
                "cast(cast(ROW(12, 12345, 123456789, 1234567890123456789, null, null, null, null) AS ROW(TINYINT, SMALLINT, INTEGER, BIGINT, TINYINT, SMALLINT, INTEGER, BIGINT)) AS JSON)",
                JSON,
                "[12,12345,123456789,1234567890123456789,null,null,null,null]");

        assertFunction(
                "CAST(ROW(CAST(3.14 AS REAL), 3.1415, 1e308, DECIMAL '3.14', DECIMAL '12345678901234567890.123456789012345678', CAST(null AS REAL), CAST(null AS DOUBLE), CAST(null AS DECIMAL)) AS JSON)",
                JSON,
                "[3.14,3.1415,1.0E308,3.14,12345678901234567890.123456789012345678,null,null,null]"
        );

        assertFunction(
                "CAST(ROW('a', 'bb', CAST(null as VARCHAR), JSON '123', JSON '3.14', JSON 'false', JSON '\"abc\"', JSON '[1, \"a\", null]', JSON '{\"a\": 1, \"b\": \"str\", \"c\": null}', JSON 'null', CAST(null AS JSON)) AS JSON)",
                JSON,
                "[\"a\",\"bb\",null,123,3.14,false,\"abc\",[1,\"a\",null],{\"a\":1,\"b\":\"str\",\"c\":null},null,null]");

        assertFunction(
                "cast(ROW(ARRAY[1, 2], ARRAY[3, null], ARRAY[], ARRAY[null, null], CAST(null AS ARRAY<BIGINT>)) AS JSON)",
                JSON,
                "[[1,2],[3,null],[],[null,null],null]");
        assertFunction(
                "cast(ROW(MAP(ARRAY['a', 'b'], ARRAY[1, 2]), MAP(ARRAY['three', 'none'], ARRAY[3, null]), MAP(), MAP(ARRAY['h1', 'h2'], ARRAY[null, null]), CAST(NULL as MAP<VARCHAR, BIGINT>)) AS JSON)",
                JSON,
                "[{\"a\":1,\"b\":2},{\"three\":3,\"none\":null},{},{\"h1\":null,\"h2\":null},null]");
        assertFunction(
                "cast(ROW(ROW(1, 2), ROW(3, CAST(null as INTEGER)), CAST(ROW(null, null) AS ROW(INTEGER, INTEGER)), null) AS JSON)",
                JSON,
                "[[1,2],[3,null],[null,null],null]");
    }
}
