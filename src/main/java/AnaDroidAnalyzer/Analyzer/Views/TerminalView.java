package AnaDroidAnalyzer.Analyzer.Views;

import java.io.PrintStream;

public class TerminalView implements View {

    PrintStream out = System.out;
    PrintStream error = System.out;

    public TerminalView() {
        out = System.out;
        error = System.out;
    }

    @Override
    public void show(String s) {
        out.println(s);
    }

    @Override
    public void showHeadedString(String header, String s) {
        out.println("---------------" + header +"------------------");
        out.println(s);
        out.println("------------------------------------------------");
    }

    @Override
    public void showMatrix(Matrix m) {

    }

    @Override
    public void showHeader() {

    }

    @Override
    public void showTestResume(String test, String consumption,String time ,String coverage) {
        out.println("---------" + " TEST CONSUMPTION" + "-----------");
        out.println("--" + " Test Name: " + test + " --");
        out.println("----------------------------------------------");
        out.println("| Test Total Consumption (J) : " + consumption + " J|");
        out.println("| Test Total Time (ms)       : " + time + " ms|");
        Double cov = Double.parseDouble(coverage);
        if (! cov.isNaN() ) {
            out.println("---------------Method Coverage of Test------------------");
            out.println("percentage: " + coverage + " %");

        }
        out.println("------------------------------------------------");

    }
}
