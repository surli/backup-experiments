package kg.apc.jmeter.reporters;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.junit.*;

import static org.junit.Assert.assertEquals;

public class LoadosophiaAggregatorTest {

    public LoadosophiaAggregatorTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testAddSample() {
        System.out.println("addSample");
        LoadosophiaAggregator instance = new LoadosophiaAggregator();
        instance.setNumSources(1);
        instance.addSample(new SampleEvent(new SampleResult(System.currentTimeMillis(), 1), ""));
        assertEquals(false, instance.haveDataToSend());
        instance.addSample(new SampleEvent(new SampleResult(System.currentTimeMillis() + 1000, 1), ""));
        instance.addSample(new SampleEvent(new SampleResult(System.currentTimeMillis() + 2000, 1), ""));
        instance.addSample(new SampleEvent(new SampleResult(System.currentTimeMillis() + 3000, 1), ""));
        instance.addSample(new SampleEvent(new SampleResult(System.currentTimeMillis() + 3000, 3), ""));
        instance.addSample(new SampleEvent(new SampleResult(System.currentTimeMillis() + 3000, 2), ""));
        instance.addSample(new SampleEvent(new SampleResult(System.currentTimeMillis() + 4000, 1), ""));
        instance.addSample(new SampleEvent(new SampleResult(System.currentTimeMillis() + 5000, 1), ""));
        instance.addSample(new SampleEvent(new SampleResult(System.currentTimeMillis() + 6000, 1), ""));
        assertEquals(true, instance.haveDataToSend());
        String str = instance.getDataToSend().toString();
        System.out.println("JSON: " + str);
        Assert.assertTrue(!str.equals("[]"));
        Assert.assertTrue(!str.equals(""));
        JSONArray test = JSONArray.fromObject(str);
        Assert.assertEquals(5, test.size());
        assertEquals(false, instance.haveDataToSend());
    }

    @Test
    public void testHaveDataToSend() {
        System.out.println("haveDataToSend");
        LoadosophiaAggregator instance = new LoadosophiaAggregator();
        boolean result = instance.haveDataToSend();
        Assert.assertEquals(false, result);
    }

    @Test
    public void testGetDataToSend() {
        System.out.println("getDataToSend");
        LoadosophiaAggregator instance = new LoadosophiaAggregator();
        String expResult = "[]";
        JSONArray result = instance.getDataToSend();
        Assert.assertEquals(expResult, result.toString());
    }

    @Test
    public void testGetQuantiles() {
        System.out.println("getQuantiles");
        Long[] rtimes = {1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L};
        JSONObject result = LoadosophiaAggregator.getQuantilesJSON(rtimes);
        Assert.assertEquals("{\"100.0\":10,\"99.0\":10,\"98.0\":10,\"95.0\":10,\"90.0\":9,\"80.0\":8,\"75.0\":8,\"50.0\":5,\"25.0\":3}", result.toString());
    }

    /**
     * Test of getQuantilesJSON method, of class LoadosophiaAggregator.
     */
    @Test
    public void testGetQuantilesJSON() {
        System.out.println("getQuantilesJSON");
        Long[] rtimes = new Long[0];
        JSONObject result = LoadosophiaAggregator.getQuantilesJSON(rtimes);
    }
}
