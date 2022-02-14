package AnaDroidAnalyzer.Results.Metrics;

import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class NumericImmutableMetric extends Metric implements IGreenSourceFormat {

    private Number value = 0.0;


    public NumericImmutableMetric(){
        super("","");
    }

    public NumericImmutableMetric(String metricId, String unit) {
        super(unit,metricId);
    }
    public NumericImmutableMetric(String metricId, String unit, Number val) {
        super(unit,metricId);
        this.value = val;
    }


    @Override
    public JSONObject toGSJSONFormat() {
        JSONObject jo = new JSONObject();
        return jo;
    }

    @Override
    public JSONObject toGSJSONFormat(String arg1) {
        return null;
    }

    @Override
    public JSONObject toGSJSONFormatTestMetric(String testResID) {
        JSONObject testMetrics = new JSONObject();
        testMetrics.put("test_results", testResID);
        testMetrics.put("metric", this.metricId.toLowerCase() );
        testMetrics.put("value_text",  value.toString());
        testMetrics.put("coeficient", this.metricUnit);
        return testMetrics;
    }


    @Override
    public JSONObject toGSJSONFormat(String arg1, String arg2) {
        return null;
    }
}

