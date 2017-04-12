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
package org.apache.metron.parsers.asa;

import java.util.Iterator;
import java.util.Map;

import org.apache.metron.parsers.sourcefire.BasicSourcefireParser;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.apache.metron.parsers.AbstractConfigTest;
import org.junit.Assert;


/**
 * <ul>
 * <li>Title: </li>
 * <li>Description: </li>
 * <li>Created: Feb 17, 2015 by: </li>
 * </ul>
 * @author $Author:  $
 * @version $Revision: 1.1 $
 */
public class GrokAsaParserTest extends AbstractConfigTest{
     /**
     * The grokAsaStrings.
     */
    private static String[] grokAsaStrings=null;
 
     /**
     * The grokAsaParser.
     */
     
    private GrokAsaParser grokAsaParser=null;
    
     /**
     * Constructs a new <code>GrokAsaParserTest</code> instance.
     * @throws Exception
     */
     
    public GrokAsaParserTest() throws Exception {
          super();  
        
    }
	/**
	 * @throws java.lang.Exception
	 */
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	public static void tearDownAfterClass() throws Exception {
		setGrokAsaStrings(null);
	}

    /* 
     * (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
	@Override
	public void setUp() throws Exception {
          super.setUp("org.apache.metron.parsers.asa.GrokAsaParserTest");
          setGrokAsaStrings(super.readTestDataFromFile(this.getConfig().getString("logFile")));
          grokAsaParser = new GrokAsaParser();		
	}

		/**
		 * 	
		 * 	
		 * @throws java.lang.Exception
		 */
		@Override
		public void tearDown() throws Exception {
			grokAsaParser = null;
		}

		/**
		 * Test method for {@link BasicSourcefireParser#parse(byte[])}.
		 */
		@SuppressWarnings({ "rawtypes" })
		public void testParse() {
		    
			for (String grokAsaString : getGrokAsaStrings()) {
				JSONObject parsed = grokAsaParser.parse(grokAsaString.getBytes()).get(0);
				Assert.assertNotNull(parsed);
			
				System.out.println(parsed);
				JSONParser parser = new JSONParser();

				Map json=null;
				try {
					json = (Map) parser.parse(parsed.toJSONString());
				} catch (ParseException e) {
					e.printStackTrace();
				}
				//Ensure JSON returned is not null/empty
				Assert.assertNotNull(json);
				
				Iterator iter = json.entrySet().iterator();
				

				while (iter.hasNext()) {
					Map.Entry entry = (Map.Entry) iter.next();
					Assert.assertNotNull(entry);
					
					String key = (String) entry.getKey();
					Assert.assertNotNull(key);
					
					String value = (String) json.get("CISCO_TAGGED_SYSLOG").toString();
					Assert.assertNotNull(value);
				}
			}
		}

		/**
		 * Returns GrokAsa Input String
		 */
		public static String[] getGrokAsaStrings() {
			return grokAsaStrings;
		}

			
		/**
		 * Sets GrokAsa Input String
		 */	
		public static void setGrokAsaStrings(String[] strings) {
			GrokAsaParserTest.grokAsaStrings = strings;
		}
	    
	    /**
	     * Returns the grokAsaParser.
	     * @return the grokAsaParser.
	     */
	    
	    public GrokAsaParser getGrokAsaParser() {
	        return grokAsaParser;
	    }


	    /**
	     * Sets the grokAsaParser.
	     * @param grokAsaParser the grokAsaParser.
	     */
	    
	    public void setGrokAsaParser(GrokAsaParser grokAsaParser) {
	    
	        this.grokAsaParser = grokAsaParser;
	    }
		
	}
