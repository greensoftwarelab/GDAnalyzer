package AnaDroidAnalyzer.Results.Metrics;

import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class NominalMutableMetric extends Metric implements IGreenSourceFormat{

    public Map<String, String> states = new HashMap<>();


    public NominalMutableMetric() {
        super("","");
        //this.states = new HashMap<>();
    }

    public NominalMutableMetric(String metricUnit, String metricId) {
        super(metricUnit,metricId);
        this.states = new HashMap<>();
    }


    @Override
    public JSONObject toGSJSONFormat() {
        return null;
    }

    @Override
    public JSONObject toGSJSONFormat(String arg2) {
        return null;
    }

    @Override
    public JSONObject toGSJSONFormatTestMetric(String testResID) {
        JSONObject testMetrics = new JSONObject();
        testMetrics.put("test_results", testResID);
        testMetrics.put("metric", this.metricId );
        testMetrics.put("value_text",  states.keySet().toArray().toString());
        testMetrics.put("coeficient", this.metricUnit);
        return testMetrics;
    }

    @Override
    public JSONObject toGSJSONFormat(String arg1, String arg2) {
        return null;
    }
}
