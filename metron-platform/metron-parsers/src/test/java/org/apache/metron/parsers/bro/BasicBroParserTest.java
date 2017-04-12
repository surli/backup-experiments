/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.metron.parsers.bro;

import junit.framework.TestCase;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Level;
import org.apache.metron.test.utils.UnitTestHelper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;

import java.util.Map;

public class BasicBroParserTest extends TestCase {

	/**
	 * The parser.
	 */
	private BasicBroParser broParser = null;
	private JSONParser jsonParser = null;

	/**
	 * Constructs a new <code>BasicBroParserTest</code> instance.
	 *
	 * @throws Exception
	 */
	public BasicBroParserTest() throws Exception {
		broParser = new BasicBroParser();
		jsonParser = new JSONParser();
	}

	/**
	 * This test is included as a gut-check about our formatting expectations using the Java JDK
	 * https://docs.oracle.com/javase/tutorial/i18n/format/decimalFormat.html
	 */
	public void testDecimalFormatAssumptions() {
		Pair[] pairs = {
						Pair.of(12345678d, "12345678.0"),
						Pair.of(12345678.0d, "12345678.0"),
						Pair.of(12345678.1d, "12345678.1"),
						Pair.of(12345678.11d, "12345678.11"),
						Pair.of(12345678.111d, "12345678.111"),
						Pair.of(12345678.1111d, "12345678.1111"),
						Pair.of(12345678.11111d, "12345678.11111"),
						Pair.of(12345678.111111d, "12345678.111111")
		};
		for (Pair pair : pairs) {
			assertEquals("Format did not match", pair.getRight(), BasicBroParser.DECIMAL_FORMAT.get().format(pair.getLeft()));
		}
	}

	public void testUnwrappedBroMessage() throws ParseException {
        String rawMessage = "{\"timestamp\":1449511228.474,\"uid\":\"CFgSLp4HgsGqXnNjZi\",\"source_ip\":\"104.130.172.191\",\"source_port\":33893,\"dest_ip\":\"69.20.0.164\",\"dest_port\":53,\"proto\":\"udp\",\"trans_id\":3514,\"rcode\":3,\"rcode_name\":\"NXDOMAIN\",\"AA\":false,\"TC\":false,\"RD\":false,\"RA\":false,\"Z\":0,\"rejected\":false,\"sensor\":\"cloudbro\",\"type\":\"dns\"}";

        JSONObject rawJson = (JSONObject)jsonParser.parse(rawMessage);

        JSONObject broJson = broParser.parse(rawMessage.getBytes()).get(0);

				String expectedBroTimestamp = "1449511228.474";
      	Assert.assertEquals(broJson.get("bro_timestamp"), expectedBroTimestamp);
        String expectedTimestamp = "1449511228474";
				Assert.assertEquals(broJson.get("timestamp").toString(), expectedTimestamp);
			  Assert.assertEquals(broJson.get("ip_src_addr").toString(), rawJson.get("source_ip").toString());
			  Assert.assertEquals(broJson.get("ip_dst_addr").toString(), rawJson.get("dest_ip").toString());
			  Assert.assertEquals(broJson.get("ip_src_port"), rawJson.get("source_port"));
        Assert.assertEquals(broJson.get("ip_dst_port"), rawJson.get("dest_port"));
        Assert.assertEquals(broJson.get("uid").toString(), rawJson.get("uid").toString());
        Assert.assertEquals(broJson.get("trans_id").toString(), rawJson.get("trans_id").toString());
        Assert.assertEquals(broJson.get("sensor").toString(), rawJson.get("sensor").toString());
        Assert.assertEquals(broJson.get("protocol").toString(), rawJson.get("type").toString());
        Assert.assertEquals(broJson.get("rcode").toString(), rawJson.get("rcode").toString());
        Assert.assertEquals(broJson.get("rcode_name").toString(), rawJson.get("rcode_name").toString());
				Assert.assertTrue(broJson.get("original_string").toString().startsWith("DNS"));
    }

	@SuppressWarnings("rawtypes")
	public void testHttpBroMessage() throws ParseException {
		String rawMessage = "{\"http\":{\"ts\":1402307733.473,\"uid\":\"CTo78A11g7CYbbOHvj\",\"id.orig_h\":\"192.249.113.37\",\"id.orig_p\":58808,\"id.resp_h\":\"72.163.4.161\",\"id.resp_p\":80,\"trans_depth\":1,\"method\":\"GET\",\"host\":\"www.cisco.com\",\"uri\":\"/\",\"user_agent\":\"curl/7.22.0 (x86_64-pc-linux-gnu) libcurl/7.22.0 OpenSSL/1.0.1 zlib/1.2.3.4 libidn/1.23 librtmp/2.3\",\"request_body_len\":0,\"response_body_len\":25523,\"status_code\":200,\"status_msg\":\"OK\",\"tags\":[],\"resp_fuids\":[\"FJDyMC15lxUn5ngPfd\"],\"resp_mime_types\":[\"text/html\"]}}";

		Map rawMessageMap = (Map) jsonParser.parse(rawMessage);
		JSONObject rawJson = (JSONObject) rawMessageMap.get(rawMessageMap.keySet().iterator().next());

		JSONObject broJson = broParser.parse(rawMessage.getBytes()).get(0);
		String expectedBroTimestamp = "1402307733.473";
		Assert.assertEquals(broJson.get("bro_timestamp"), expectedBroTimestamp);
    String expectedTimestamp = "1402307733473";
		Assert.assertEquals(broJson.get("timestamp").toString(), expectedTimestamp);
		Assert.assertEquals(broJson.get("ip_src_addr").toString(), rawJson.get("id.orig_h").toString());
		Assert.assertEquals(broJson.get("ip_dst_addr").toString(), rawJson.get("id.resp_h").toString());
		Assert.assertEquals(broJson.get("ip_src_port").toString(), rawJson.get("id.orig_p").toString());
		Assert.assertEquals(broJson.get("ip_dst_port").toString(), rawJson.get("id.resp_p").toString());
		Assert.assertTrue(broJson.get("original_string").toString().startsWith(rawMessageMap.keySet().iterator().next().toString().toUpperCase()));

		Assert.assertEquals(broJson.get("uid").toString(), rawJson.get("uid").toString());
		Assert.assertEquals(broJson.get("method").toString(), rawJson.get("method").toString());
		Assert.assertEquals(broJson.get("host").toString(), rawJson.get("host").toString());
		Assert.assertEquals(broJson.get("resp_mime_types").toString(), rawJson.get("resp_mime_types").toString());
	}

	/**
	 * Ensure we are able to parse values with fewer than 13 digits, including the millis after the decimal.
	 * This test verifies we handle parsing timestamps in situations where, for instance, 123.000000 is represented
	 * more compactly as 123.0
	 */
	@SuppressWarnings("rawtypes")
	public void testHttpBroMessageWithZeroDecimalTruncation() throws ParseException {
		{
			String rawMessage = "{\"http\": {\"ts\":1467657279,\"uid\":\"CMYLzP3PKiwZAgBa51\",\"id.orig_h\":\"192.168.138.158\",\"id.orig_p\":49206,\"id.resp_h\":\"95.163.121.204\"," +
							"\"id.resp_p\":80,\"trans_depth\":2,\"method\":\"GET\",\"host\":\"7oqnsnzwwnm6zb7y.gigapaysun.com\",\"uri\":\"/img/flags/it.png\",\"referrer\":\"http://7oqnsnzwwnm6zb7y.gigapaysun.com/11iQmfg\",\"user_agent\":\"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0)\",\"request_body_len\":0,\"response_body_len\":552,\"status_code\":200,\"status_msg\":\"OK\",\"tags\":[],\"resp_fuids\":[\"F3m7vB2RjUe4n01aqj\"],\"resp_mime_types\":[\"image/png\"]}}";

			Map rawMessageMap = (Map) jsonParser.parse(rawMessage);
			JSONObject rawJson = (JSONObject) rawMessageMap.get(rawMessageMap.keySet().iterator().next());

			JSONObject broJson = broParser.parse(rawMessage.getBytes()).get(0);
			String expectedTimestamp = "1467657279000";
			Assert.assertEquals(broJson.get("timestamp").toString(), expectedTimestamp);
			String expectedBroTimestamp = "1467657279.0";
			Assert.assertEquals(broJson.get("bro_timestamp").toString(), expectedBroTimestamp);
		}
		{
			String rawMessage = "{\"http\": {\"ts\":1467657279.0,\"uid\":\"CMYLzP3PKiwZAgBa51\",\"id.orig_h\":\"192.168.138.158\",\"id.orig_p\":49206,\"id.resp_h\":\"95.163.121.204\"," +
							"\"id.resp_p\":80,\"trans_depth\":2,\"method\":\"GET\",\"host\":\"7oqnsnzwwnm6zb7y.gigapaysun.com\",\"uri\":\"/img/flags/it.png\",\"referrer\":\"http://7oqnsnzwwnm6zb7y.gigapaysun.com/11iQmfg\",\"user_agent\":\"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0)\",\"request_body_len\":0,\"response_body_len\":552,\"status_code\":200,\"status_msg\":\"OK\",\"tags\":[],\"resp_fuids\":[\"F3m7vB2RjUe4n01aqj\"],\"resp_mime_types\":[\"image/png\"]}}";

			Map rawMessageMap = (Map) jsonParser.parse(rawMessage);
			JSONObject rawJson = (JSONObject) rawMessageMap.get(rawMessageMap.keySet().iterator().next());

			JSONObject broJson = broParser.parse(rawMessage.getBytes()).get(0);
			String expectedTimestamp = "1467657279000";
			Assert.assertEquals(broJson.get("timestamp").toString(), expectedTimestamp);
			String expectedBroTimestamp = "1467657279.0";
			Assert.assertEquals(broJson.get("bro_timestamp").toString(), expectedBroTimestamp);
		}
		{
			String rawMessage = "{\"http\": {\"ts\":1467657279.1,\"uid\":\"CMYLzP3PKiwZAgBa51\",\"id.orig_h\":\"192.168.138.158\",\"id.orig_p\":49206,\"id.resp_h\":\"95.163.121.204\"," +
							"\"id.resp_p\":80,\"trans_depth\":2,\"method\":\"GET\",\"host\":\"7oqnsnzwwnm6zb7y.gigapaysun.com\",\"uri\":\"/img/flags/it.png\",\"referrer\":\"http://7oqnsnzwwnm6zb7y.gigapaysun.com/11iQmfg\",\"user_agent\":\"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0)\",\"request_body_len\":0,\"response_body_len\":552,\"status_code\":200,\"status_msg\":\"OK\",\"tags\":[],\"resp_fuids\":[\"F3m7vB2RjUe4n01aqj\"],\"resp_mime_types\":[\"image/png\"]}}";

			Map rawMessageMap = (Map) jsonParser.parse(rawMessage);
			JSONObject rawJson = (JSONObject) rawMessageMap.get(rawMessageMap.keySet().iterator().next());

			JSONObject broJson = broParser.parse(rawMessage.getBytes()).get(0);
			String expectedTimestamp = "1467657279100";
			Assert.assertEquals(broJson.get("timestamp").toString(), expectedTimestamp);
			String expectedBroTimestamp = "1467657279.1";
			Assert.assertEquals(broJson.get("bro_timestamp").toString(), expectedBroTimestamp);
		}
		{
			String rawMessage = "{\"http\": {\"ts\":1467657279.11,\"uid\":\"CMYLzP3PKiwZAgBa51\",\"id.orig_h\":\"192.168.138.158\",\"id.orig_p\":49206,\"id.resp_h\":\"95.163.121.204\"," +
							"\"id.resp_p\":80,\"trans_depth\":2,\"method\":\"GET\",\"host\":\"7oqnsnzwwnm6zb7y.gigapaysun.com\",\"uri\":\"/img/flags/it.png\",\"referrer\":\"http://7oqnsnzwwnm6zb7y.gigapaysun.com/11iQmfg\",\"user_agent\":\"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0)\",\"request_body_len\":0,\"response_body_len\":552,\"status_code\":200,\"status_msg\":\"OK\",\"tags\":[],\"resp_fuids\":[\"F3m7vB2RjUe4n01aqj\"],\"resp_mime_types\":[\"image/png\"]}}";

			Map rawMessageMap = (Map) jsonParser.parse(rawMessage);
			JSONObject rawJson = (JSONObject) rawMessageMap.get(rawMessageMap.keySet().iterator().next());

			JSONObject broJson = broParser.parse(rawMessage.getBytes()).get(0);
			String expectedTimestamp = "1467657279110";
			Assert.assertEquals(broJson.get("timestamp").toString(), expectedTimestamp);
			String expectedBroTimestamp = "1467657279.11";
			Assert.assertEquals(broJson.get("bro_timestamp").toString(), expectedBroTimestamp);
		}
	}

	@SuppressWarnings("rawtypes")
	public void testHttpDecimalBroMessage() throws ParseException {
		String rawMessage = "{\"http\":{\"ts\":1457149494.166991,\"uid\":\"CTo78A11g7CYbbOHvj\",\"id.orig_h\":\"192.249.113.37\",\"id.orig_p\":58808,\"id.resp_h\":\"72.163.4.161\",\"id.resp_p\":80,\"trans_depth\":1,\"method\":\"GET\",\"host\":\"www.cisco.com\",\"uri\":\"/\",\"user_agent\":\"curl/7.22.0 (x86_64-pc-linux-gnu) libcurl/7.22.0 OpenSSL/1.0.1 zlib/1.2.3.4 libidn/1.23 librtmp/2.3\",\"request_body_len\":0,\"response_body_len\":25523,\"status_code\":200,\"status_msg\":\"OK\",\"tags\":[],\"resp_fuids\":[\"FJDyMC15lxUn5ngPfd\"],\"resp_mime_types\":[\"text/html\"]}}";
		Map rawMessageMap = (Map) jsonParser.parse(rawMessage);
		JSONObject rawJson = (JSONObject) rawMessageMap.get(rawMessageMap.keySet().iterator().next());

		JSONObject broJson = broParser.parse(rawMessage.getBytes()).get(0);
		String expectedBroTimestamp = "1457149494.166991";
		Assert.assertEquals(broJson.get("bro_timestamp"), expectedBroTimestamp);
		String expectedTimestamp = "1457149494166";
		Assert.assertEquals(broJson.get("timestamp").toString(), expectedTimestamp);
		Assert.assertEquals(broJson.get("ip_src_addr").toString(), rawJson.get("id.orig_h").toString());
		Assert.assertEquals(broJson.get("ip_dst_addr").toString(), rawJson.get("id.resp_h").toString());
		Assert.assertEquals(broJson.get("ip_src_port").toString(), rawJson.get("id.orig_p").toString());
		Assert.assertEquals(broJson.get("ip_dst_port").toString(), rawJson.get("id.resp_p").toString());
		Assert.assertTrue(broJson.get("original_string").toString().startsWith(rawMessageMap.keySet().iterator().next().toString().toUpperCase()));

		Assert.assertEquals(broJson.get("uid").toString(), rawJson.get("uid").toString());
		Assert.assertEquals(broJson.get("method").toString(), rawJson.get("method").toString());
		Assert.assertEquals(broJson.get("host").toString(), rawJson.get("host").toString());
		Assert.assertEquals(broJson.get("resp_mime_types").toString(), rawJson.get("resp_mime_types").toString());
	}


	@SuppressWarnings("rawtypes")
	public void testDnsBroMessage() throws ParseException {
		String rawMessage = "{\"dns\":{\"ts\":1402308259.609,\"uid\":\"CuJT272SKaJSuqO0Ia\",\"id.orig_h\":\"10.122.196.204\",\"id.orig_p\":33976,\"id.resp_h\":\"144.254.71.184\",\"id.resp_p\":53,\"proto\":\"udp\",\"trans_id\":62418,\"query\":\"www.cisco.com\",\"qclass\":1,\"qclass_name\":\"C_INTERNET\",\"qtype\":28,\"qtype_name\":\"AAAA\",\"rcode\":0,\"rcode_name\":\"NOERROR\",\"AA\":true,\"TC\":false,\"RD\":true,\"RA\":true,\"Z\":0,\"answers\":[\"www.cisco.com.akadns.net\",\"origin-www.cisco.com\",\"2001:420:1201:2::a\"],\"TTLs\":[3600.0,289.0,14.0],\"rejected\":false}}";

		Map rawMessageMap = (Map) jsonParser.parse(rawMessage);
		JSONObject rawJson = (JSONObject) rawMessageMap.get(rawMessageMap.keySet().iterator().next());

		JSONObject broJson = broParser.parse(rawMessage.getBytes()).get(0);
		String expectedBroTimestamp = "1402308259.609";
		Assert.assertEquals(broJson.get("bro_timestamp"), expectedBroTimestamp);
		String expectedTimestamp = "1402308259609";
		Assert.assertEquals(broJson.get("timestamp").toString(), expectedTimestamp);
		Assert.assertEquals(broJson.get("ip_src_addr").toString(), rawJson.get("id.orig_h").toString());
		Assert.assertEquals(broJson.get("ip_dst_addr").toString(), rawJson.get("id.resp_h").toString());
		Assert.assertEquals(broJson.get("ip_src_port").toString(), rawJson.get("id.orig_p").toString());
		Assert.assertEquals(broJson.get("ip_dst_port").toString(), rawJson.get("id.resp_p").toString());
		Assert.assertTrue(broJson.get("original_string").toString().startsWith(rawMessageMap.keySet().iterator().next().toString().toUpperCase()));

		Assert.assertEquals(broJson.get("qtype").toString(), rawJson.get("qtype").toString());
		Assert.assertEquals(broJson.get("trans_id").toString(), rawJson.get("trans_id").toString());
	}

	@SuppressWarnings("rawtypes")
	public void testFilesBroMessage() throws ParseException {
		String rawMessage = "{\"files\":{\"analyzers\": [\"X509\",\"MD5\",\"SHA1\"],\"conn_uids\":[\"C4tygJ3qxJBEJEBCeh\"],\"depth\": 0,\"duration\": 0.0,\"fuid\":\"FZEBC33VySG0nHSoO9\",\"is_orig\": false,\"local_orig\": false,\"md5\": \"eba37166385e3ef42464ed9752e99f1b\",\"missing_bytes\": 0,\"overflow_bytes\": 0,\"rx_hosts\": [\"10.220.15.205\"],\"seen_bytes\": 1136,\"sha1\": \"73e42686657aece354fbf685712361658f2f4357\",\"source\": \"SSL\",\"timedout\": false,\"ts\": 1425845251.334,\"tx_hosts\": [\"68.171.237.7\"]}}";

		Map rawMessageMap = (Map) jsonParser.parse(rawMessage);
		JSONObject rawJson = (JSONObject) rawMessageMap.get(rawMessageMap.keySet().iterator().next());

		JSONObject broJson = broParser.parse(rawMessage.getBytes()).get(0);
		String expectedBroTimestamp = "1425845251.334";
		Assert.assertEquals(broJson.get("bro_timestamp"), expectedBroTimestamp);
		String expectedTimestamp = "1425845251334";
		Assert.assertEquals(broJson.get("timestamp").toString(), expectedTimestamp);
		Assert.assertEquals(broJson.get("ip_src_addr").toString(), ((JSONArray)rawJson.get("tx_hosts")).get(0).toString());
		Assert.assertEquals(broJson.get("ip_dst_addr").toString(), ((JSONArray)rawJson.get("rx_hosts")).get(0).toString());
		Assert.assertTrue(broJson.get("original_string").toString().startsWith(rawMessageMap.keySet().iterator().next().toString().toUpperCase()));

		Assert.assertEquals(broJson.get("fuid").toString(), rawJson.get("fuid").toString());
		Assert.assertEquals(broJson.get("md5").toString(), rawJson.get("md5").toString());
		Assert.assertEquals(broJson.get("analyzers").toString(), rawJson.get("analyzers").toString());
	}

	@SuppressWarnings("rawtypes")
	public void testProtocolKeyCleanedUp() throws ParseException {
		String rawMessage = "{\"ht*tp\":{\"ts\":1402307733.473,\"uid\":\"CTo78A11g7CYbbOHvj\",\"id.orig_h\":\"192.249.113.37\",\"id.orig_p\":58808,\"id.resp_h\":\"72.163.4.161\",\"id.resp_p\":80,\"trans_depth\":1,\"method\":\"GET\",\"host\":\"www.cisco.com\",\"uri\":\"/\",\"user_agent\":\"curl/7.22.0 (x86_64-pc-linux-gnu) libcurl/7.22.0 OpenSSL/1.0.1 zlib/1.2.3.4 libidn/1.23 librtmp/2.3\",\"request_body_len\":0,\"response_body_len\":25523,\"status_code\":200,\"status_msg\":\"OK\",\"tags\":[],\"resp_fuids\":[\"FJDyMC15lxUn5ngPfd\"],\"resp_mime_types\":[\"text/html\"]}}";

		Map rawMessageMap = (Map) jsonParser.parse(rawMessage);
		JSONObject rawJson = (JSONObject) rawMessageMap.get(rawMessageMap.keySet().iterator().next());

		JSONObject broJson = broParser.parse(rawMessage.getBytes()).get(0);
		String expectedBroTimestamp = "1402307733.473";
		Assert.assertEquals(broJson.get("bro_timestamp"), expectedBroTimestamp);
		String expectedTimestamp = "1402307733473";
		Assert.assertEquals(broJson.get("timestamp").toString(), expectedTimestamp);
		Assert.assertEquals(broJson.get("ip_src_addr").toString(), rawJson.get("id.orig_h").toString());
		Assert.assertTrue(broJson.get("original_string").toString().startsWith("HTTP"));
	}

	public void testBadMessage()  throws ParseException{
		UnitTestHelper.setLog4jLevel(BasicBroParser.class, Level.FATAL);
		try {
			broParser.parse("{ \"foo\" : \"bar\"}".getBytes());
			Assert.fail("Should have marked this as a bad message.");
		}
		catch(IllegalStateException ise) {

		}
		//non json
		try {
			broParser.parse("foo bar".getBytes());
			Assert.fail("Should have marked this as a bad message.");
		}
		catch(IllegalStateException ise) {

		}
		UnitTestHelper.setLog4jLevel(BasicBroParser.class, Level.ERROR);
	}
}
