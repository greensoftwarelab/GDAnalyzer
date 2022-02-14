package AnaDroidAnalyzer.Results.Metrics;

public class Issue extends Metric {
    public String message = "";
    public String severity = "";
    public int priority = 0;
    public String summary = "";
    public String file = "";
    public int line = 0;


    public Issue(String metricUnit, String metricId) {
        super(metricUnit, metricId);
    }
}
