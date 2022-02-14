package AnaDroidAnalyzer.Results.Metrics;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Map;

public class MetricDistribution {

    public Number minimun = 0;
    public Number maximun = 0;
    public Number average = 0;


    public MetricDistribution() {
    }

    @Override
    public String toString() {
        return "MetricDistribution{" +
                "minimun=" + minimun +
                ", average=" + average +
                ", maximun=" + maximun +
                '}';
    }

    public Double[] asArray(){
        Double [] a = new Double[3];
        a[0]= minimun.doubleValue();
        a[1]= average.doubleValue();
        a[2] = maximun.doubleValue();
        return a;
    }

    public MetricDistribution(Map<? extends Number,? extends Number> samples) {
        Number min = Integer.MAX_VALUE;
        Number max = Integer.MIN_VALUE;
        Number totalSamples = samples.size();
        double total = 0;
        if (samples.isEmpty()){
            this.average =0;
            this.maximun = 0;
            this.minimun = 0;
        }
        else {
            for (Number number :samples.values() ) {
                if (number.doubleValue() > max.doubleValue()){
                    max = number;
                }
                else if (number.doubleValue() < min.doubleValue()){
                    min = number;
                }
                total+= number.doubleValue();
            }
            this.average = total / totalSamples.doubleValue();
            this.maximun = max.doubleValue();
            this.minimun = min.doubleValue();
        }

    }

    public Object toInlineString() {
        return  round( minimun.doubleValue() , 2)+ "," +
                    round( average.doubleValue() , 2) + "," +
                    round( maximun.doubleValue() , 2);
    }

    public static BigDecimal round(Double d, int decimalPlace) {
        if (d.isNaN()){
            d = 0.0;
        }
        BigDecimal bd = new BigDecimal(Double.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd;
    }
}
