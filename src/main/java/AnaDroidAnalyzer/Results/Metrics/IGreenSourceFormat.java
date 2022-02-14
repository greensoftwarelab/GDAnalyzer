package AnaDroidAnalyzer.Results.Metrics;

import org.json.simple.JSONObject;

public interface IGreenSourceFormat {

    JSONObject toGSJSONFormat();

    JSONObject toGSJSONFormat(String arg1);

    JSONObject toGSJSONFormatTestMetric(String arg1);

    JSONObject toGSJSONFormat(String arg1, String arg2);

}
