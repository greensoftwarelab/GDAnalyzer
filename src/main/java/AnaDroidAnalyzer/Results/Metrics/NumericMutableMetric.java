package AnaDroidAnalyzer.Results.Metrics;

import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class NumericMutableMetric extends Metric implements IGreenSourceFormat {

    public Map< Number, Number> metricSamples = new HashMap<>(); // pairs   timestamp -> obtained value
    public MetricDistribution metricDistribution = new MetricDistribution();



    public NumericMutableMetric(){
        super("","");
    }

    public NumericMutableMetric(String metricId, String unit) {
        super(unit,metricId);
        this.metricId = metricId;
        this.metricUnit = unit;
        this.metricSamples = new HashMap<>();
        this.metricDistribution = new MetricDistribution();
    }

    public NumericMutableMetric(String metricId, String unit, Map< ? extends Number, ? extends Number> metricSamples) {
        super(unit,metricId);
        this.metricSamples = new HashMap<>();
        this.metricSamples.putAll(metricSamples);
        this.metricDistribution =  this.distribute(metricSamples);
    }


    private MetricDistribution distribute(Map<? extends Number, ? extends Number> metricSamples) {
        return  new MetricDistribution(metricSamples);
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
        testMetrics.put("value_text",  metricDistribution.toInlineString());
        testMetrics.put("coeficient", this.metricUnit);
        return testMetrics;
    }


    @Override
    public JSONObject toGSJSONFormat(String arg1, String arg2) {
        return null;
    }
}

