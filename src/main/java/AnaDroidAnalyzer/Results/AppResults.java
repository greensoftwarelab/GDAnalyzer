package AnaDroidAnalyzer.Results;

import java.util.ArrayList;
import java.util.List;

public class AppResults {
    public List<String> headers = new ArrayList<>();
    public List<String> metricsName = new ArrayList<>();


    public AppResults() {
        headers.add("Class");
        headers.add("Name");
        headers.add("Times invoked");
        headers.add("NrInstructions");
        headers.add("Length");
        headers.add("APIs");
        headers.add("Return");
        headers.add("Args"); //l.add("EnergyGreadyAPIS");
    }


}
