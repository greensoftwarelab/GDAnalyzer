package AnaDroidAnalyzer.Results.Metrics;

import AnaDroidAnalyzer.Utils.Utils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TrepnCSVMetric extends Metric {


    private int time_csv_column = 0;
    private int value_csv_column = 0;
    private TreeMap<Integer, Number> samples = new TreeMap<>() ;




    public TrepnCSVMetric(String metricUnit, String metricId) {
        super(metricUnit, metricId);
    }

    public TrepnCSVMetric(String row) {
        super("", row);
        Pattern regex = Pattern.compile("\\[(.*?)\\]");
        Matcher regexMatcher = regex.matcher(row);
        if (regexMatcher.find()){
            metricUnit = regexMatcher.group(1);
        }

    }

    public int getTime_csv_column() {
        return time_csv_column;
    }

    public void setTime_csv_column(int time_csv_column) {
        this.time_csv_column = time_csv_column;
    }

    public int getValue_csv_column() {
        return value_csv_column;
    }

    public void setValue_csv_column(int value_csv_column) {
        this.value_csv_column = value_csv_column;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrepnCSVMetric that = (TrepnCSVMetric) o;
        return metricId.equals(that.metricId) && metricUnit.equals(that.metricUnit) &&  time_csv_column == that.time_csv_column &&
                value_csv_column == that.value_csv_column;
    }
    @Override
    public int hashCode() {
        return  (metricId + metricUnit + time_csv_column + value_csv_column ).hashCode();
    }

    public void addSample( int timestamp, Number value){
        samples.put(timestamp,value);
    }

    public Number getSample( int timestamp){
        return samples.get(timestamp);
    }

    public TreeMap<Integer, Number> getSamplesMap() {
        return samples;
    }
}

