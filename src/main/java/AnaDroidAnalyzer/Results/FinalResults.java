package AnaDroidAnalyzer.Results;

import AnaDroidAnalyzer.Analyzer.Views.Matrix;

public class FinalResults {
    public Matrix<String> matrix;

    public FinalResults(String [][] matrix) {
        this.matrix = new Matrix<>(matrix);
    }
}
