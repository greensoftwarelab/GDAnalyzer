package AnaDroidAnalyzer.Results.Metrics;

import org.json.simple.JSONObject;

public class NominalImmutableMetric extends Metric {


    private String value = "";

    public NominalImmutableMetric(String metricId, String metricUnit) {
        super(metricUnit, metricId);
    }

    public NominalImmutableMetric( String metricId,String metricUnit, String value) {
        super(metricUnit, metricId);
        this.value = value;
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
        testMetrics.put("value_text",  value);
        testMetrics.put("coeficient", this.metricUnit);
        return testMetrics;
    }


    @Override
    public JSONObject toGSJSONFormat(String arg1, String arg2) {
        return null;
    }
}
