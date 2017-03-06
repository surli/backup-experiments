package kg.apc.jmeter.reporters;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import java.text.SimpleDateFormat;
import java.util.*;

public class LoadosophiaAggregator {

    private static final Logger log = LoggingManager.getLoggerForClass();
    private SortedMap<Long, List<SampleEvent>> buffer = new TreeMap<>();
    private static final long SEND_SECONDS = 5;
    private long lastAggregatedTime = 0;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    private int numSources = 0;

    public void addSample(SampleEvent res) {
        Long time = res.getResult().getEndTime() / 1000;
        if (!buffer.containsKey(time)) {
            // we need to create new sec list
            if (time <= lastAggregatedTime) {
                // a problem with times sequence - taking last available
                log.debug("Got time " + time + " <= " + lastAggregatedTime);
                for (Long aLong : buffer.keySet()) {
                    time = aLong;
                }
            }
            buffer.put(time, new LinkedList<SampleEvent>());
        }
        buffer.get(time).add(res);
    }

    public boolean haveDataToSend() {
        return buffer.size() > getBufLen() + 1;
    }

    private long getBufLen() {
        return SEND_SECONDS * numSources;
    }

    public JSONArray getDataToSend() {
        JSONArray data = new JSONArray();
        Iterator<Long> it = buffer.keySet().iterator();
        int cnt = 0;
        while (cnt < getBufLen() && it.hasNext()) {
            Long sec = it.next();
            List<SampleEvent> raw = buffer.get(sec);
            data.add(getAggregateSecond(sec, raw));
            it.remove();
            cnt++;
        }
        return data;
    }

    private JSONObject getAggregateSecond(Long sec, List<SampleEvent> raw) {
        /*
         "rc": item.http_codes,
         "net": item.net_codes
         */
        JSONObject result = new JSONObject();
        this.lastAggregatedTime = sec;
        Date ts = new Date(sec * 1000);
        log.debug("Aggregating " + sec);
        result.put("ts", format.format(ts));

        Map<String, Integer> threads = new HashMap<>();
        int avg_rt = 0;
        Long[] rtimes = new Long[raw.size()];
        String[] rcodes = new String[raw.size()];
        int cnt = 0;
        int failedCount = 0;
        for (SampleEvent evt : raw) {
            SampleResult res = evt.getResult();

            if (!threads.containsKey(evt.getHostname())) {
                threads.put(evt.getHostname(), 0);
            }
            threads.put(evt.getHostname(), res.getAllThreads());

            avg_rt += res.getTime();
            rtimes[cnt] = res.getTime();
            rcodes[cnt] = res.getResponseCode();
            if (!res.isSuccessful()) {
                failedCount++;
            }
            cnt++;
        }

        long tsum = 0;
        for (Integer tcount : threads.values()) {
            tsum += tcount;
        }
        result.put("rps", cnt);
        result.put("threads", tsum);
        result.put("avg_rt", avg_rt / cnt);
        result.put("quantiles", getQuantilesJSON(rtimes));
        result.put("net", getNetJSON(failedCount, cnt - failedCount));
        result.put("rc", getRCJSON(rcodes));
        result.put("planned_rps", 0); // JMeter has no such feature like Yandex.Tank
        return result;
    }

    public static JSONObject getQuantilesJSON(Long[] rtimes) {
        JSONObject result = new JSONObject();
        Arrays.sort(rtimes);

        double[] quantiles = {0.25, 0.50, 0.75, 0.80, 0.90, 0.95, 0.98, 0.99, 1.00};

        Stack<Long> timings = new Stack<>();
        timings.addAll(Arrays.asList(rtimes));
        double level = 1.0;
        Object timing = 0;
        for (int qn = quantiles.length - 1; qn >= 0; qn--) {
            double quan = quantiles[qn];
            while (level >= quan && !timings.empty()) {
                timing = timings.pop();
                level -= 1.0 / rtimes.length;
            }
            result.element(String.valueOf(quan * 100), timing);
        }

        return result;
    }

    private JSONObject getNetJSON(int failedCount, int succCount) {
        JSONObject result = new JSONObject();
        result.put("0", succCount);
        result.put("1", failedCount);
        return result;
    }

    private JSONObject getRCJSON(String[] rcodes) {
        JSONObject result = new JSONObject();
        for (String rcode : rcodes) {
            int oldval = 0;
            if (result.containsKey(rcode)) {
                oldval = (Integer) result.get(rcode);
            }
            result.put(rcode, oldval + 1);

        }
        return result;
    }

    public void setNumSources(int numSources) {
        this.numSources = numSources;
    }
}
