package AnaDroidAnalyzer.Analyzer;

import AnaDroidAnalyzer.Utils.Pair;
import AnaDroidAnalyzer.Results.TrepnResults;

import java.util.List;

public interface PowerLogAnalyzer {

     void showFinalStatistics();

     void analyze(List<String> files );

}
