package AnaDroidAnalyzer.Analyzer.Views;

public interface View {
    void show(String s);
    void showHeadedString(String header, String s);
    void showMatrix(Matrix m);
    void showHeader();
    void showTestResume(String test, String consumption, String time ,String coverage);
}
