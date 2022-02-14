package AnaDroidAnalyzer.Results.Metrics;

import org.json.simple.JSONObject;

import java.util.*;

public class Metric implements IGreenSourceFormat {

    public String metricUnit = "%";
    public String metricId = "Unamed NumericMetric";
    public static HashMap<TestMetricCategory, HashSet<MetricValidator>> testMetricValidations;

    {
        testMetricValidations = new HashMap<>();
        testMetricValidations.put(TestMetricCategory.CPU_LOAD, new HashSet<MetricValidator>(Arrays.asList(
                MetricValidator.NOTNULL,
                MetricValidator.PERCENTAGE )) );
        testMetricValidations.put(TestMetricCategory.GPU_LOAD, new HashSet<MetricValidator>(Arrays.asList(
                MetricValidator.NOTNULL,
                MetricValidator.PERCENTAGE )) );
        testMetricValidations.put(TestMetricCategory.CPU_FREQUENCY, new HashSet<MetricValidator>(Arrays.asList(
                MetricValidator.NOTNULL,
                MetricValidator.POSITIVE )) );
        testMetricValidations.put(TestMetricCategory.GPU_FREQUENCY, new HashSet<MetricValidator>(Arrays.asList(
                MetricValidator.NOTNULL,
                MetricValidator.POSITIVE )) );
        testMetricValidations.put(TestMetricCategory.WIFI_RSSI_LEVEL, new HashSet<MetricValidator>(Arrays.asList(
                MetricValidator.NOTNULL)) );
        testMetricValidations.put(TestMetricCategory.WIFI_STATE, new HashSet<MetricValidator>(Arrays.asList(
                MetricValidator.NOTNULL,
                MetricValidator.NATURAL )) );
        testMetricValidations.put(TestMetricCategory.SCREEN_STATE, new HashSet<MetricValidator>(Arrays.asList(
                MetricValidator.NOTNULL,
                MetricValidator.BINARY )) );
        testMetricValidations.put(TestMetricCategory.BLUETOOTH_STATE, new HashSet<MetricValidator>(Arrays.asList(
                MetricValidator.NOTNULL,
                MetricValidator.NATURAL )) );
        testMetricValidations.put(TestMetricCategory.BATTERY_CHARGING, new HashSet<MetricValidator>(Arrays.asList(
                MetricValidator.NOTNULL,
                MetricValidator.BINARY )) );
        testMetricValidations.put(TestMetricCategory.BATTERY_STATUS, new HashSet<MetricValidator>(Arrays.asList(
                MetricValidator.NOTNULL,
                MetricValidator.NATURAL )) );
        testMetricValidations.put(TestMetricCategory.GPS_STATE, new HashSet<MetricValidator>(Arrays.asList(
                MetricValidator.NOTNULL,
                MetricValidator.NATURAL )) );
        testMetricValidations.put(TestMetricCategory.MEMORY, new HashSet<MetricValidator>(Arrays.asList(
                MetricValidator.NOTNULL,
                MetricValidator.POSITIVE )) );
        testMetricValidations.put(TestMetricCategory.METHOD_COVERAGE, new HashSet<MetricValidator>(Arrays.asList(
                MetricValidator.NOTNULL,
                MetricValidator.PERCENTAGE )) );
        testMetricValidations.put(TestMetricCategory.ELAPSED_TIME, new HashSet<MetricValidator>(Arrays.asList(
                MetricValidator.NOTNULL,
                MetricValidator.POSITIVE )) );
        testMetricValidations.put(TestMetricCategory.ENERGY_CONSUMED, new HashSet<MetricValidator>(Arrays.asList(
                MetricValidator.NOTNULL,
                MetricValidator.POSITIVE )) );
        testMetricValidations.put(TestMetricCategory.MOBILE_DATA_STATE, new HashSet<MetricValidator>(Arrays.asList(
                MetricValidator.NOTNULL,
                MetricValidator.NATURAL )) );
        testMetricValidations.put(TestMetricCategory.BATTERY_POWER, new HashSet<MetricValidator>(Arrays.asList(
                MetricValidator.NOTNULL,
                MetricValidator.POSITIVE )) );
        testMetricValidations.put(TestMetricCategory.BATTERY_POWER_DELTA, new HashSet<MetricValidator>(Arrays.asList(
                MetricValidator.NOTNULL)) );
        testMetricValidations.put(TestMetricCategory.OTHER, new HashSet<MetricValidator>( ));
    }

    public Metric(String metricUnit, String metricId) {
        this.metricUnit = metricUnit;
        this.metricId = metricId;
    }


     public static boolean isValidTestMetric( TestMetricCategory testMetricCategory, List<Object> objects_to_validate ){
        boolean is_valid = true;
        Set<MetricValidator> validationSet = testMetricValidations.get(testMetricCategory);
         for (MetricValidator validator : validationSet){
             switch (validator){
                 case INT:
                     try {
                         objects_to_validate.forEach( x -> Integer.parseInt(x.toString()) );
                     }
                     catch (NumberFormatException e){
                         return false;
                     }
                     break;
                 case NATURAL:
                     try {
                         long size = objects_to_validate.stream().map( x -> Integer.parseInt(x.toString()) ).filter(z -> z>0).count();
                         if (size!=objects_to_validate.size()){
                             return false;
                         }
                     }
                     catch (NumberFormatException e){
                         return false;
                     }
                     break;
                 case NOTNULL:
                     long nulls = objects_to_validate.stream().filter(z -> z==null).count();
                     if (nulls>0){
                         return false;
                     }
                     break;
                 case BINARY:
                     try {
                         long non_bin = objects_to_validate.stream().map( x -> Integer.parseInt(x.toString()) ).filter(z -> z!=0 && z!=1).count();
                         if (non_bin>0){
                             return false;
                         }
                     }
                     catch (NumberFormatException e){
                         return false;
                     }
                     break;
                 case POSITIVE:
                     try {
                         long non_positives = objects_to_validate.stream().map( x -> Double.parseDouble(x.toString()) ).filter(z -> z<0).count();
                         if (non_positives>0){
                             return false;
                         }
                     }
                     catch (NumberFormatException e){
                         return false;
                     }
                     break;
                 case NEGATIVE:
                     try {
                         long non_negatives = objects_to_validate.stream().map( x -> Double.parseDouble(x.toString()) ).filter(z -> z>0).count();
                         if (non_negatives>0){
                             return false;
                         }
                     }
                     catch (NumberFormatException e){
                         return false;
                     }
                     break;
                 case PERCENTAGE:
                     try {
                         long howMany01 = objects_to_validate.stream().map( x -> Double.parseDouble(x.toString()) ).filter(z -> z<=1 && z>=0 ).count();
                         long howMany0100 = objects_to_validate.stream().map( x -> Double.parseDouble(x.toString()) ).filter(z -> z<=100 || z>0 ).count();
                         if (howMany01!=objects_to_validate.size() && howMany0100 != objects_to_validate.size() ){
                             return false;
                         }
                     }
                     catch (NumberFormatException e){
                         return false;
                     }
                 case OTHER:
                     break;
                 default:
                     break;

             }
         }
         return is_valid;
    }

    @Override
    public JSONObject toGSJSONFormat() {
        return null;
    }

    @Override
    public JSONObject toGSJSONFormat(String arg1) {
        return null;
    }

    @Override
    public JSONObject toGSJSONFormatTestMetric(String arg1) {
        return null;
    }

    @Override
    public JSONObject toGSJSONFormat(String arg1, String arg2) {
        return null;
    }

    public enum MetricValidator {
        NOMINAL (0),
        NATURAL(1),
        INT(2),
        POSITIVE (3),
        NEGATIVE (4),
        PERCENTAGE (5),
        BINARY (6),
        NOTNULL (7),
        OTHER (8);

        private int code;

        MetricValidator(int code) {
            this.code = code;
        }
        public int getCode() {
            return code;
        }

        public boolean validate( Number value){
            switch (this){
                case NATURAL:
                    return value.intValue()>=0;
                default:
                    return true;
            }
        }
    }

    public enum TestMetricCategory {
        CPU_LOAD(0),
        CPU_FREQUENCY(1),
        GPU_LOAD(2),
        GPU_FREQUENCY(3),
        GPS_STATE(4),
        BLUETOOTH_STATE(5),
        MEMORY(6),
        WIFI_RSSI_LEVEL(7),
        BATTERY_STATUS(8),
        BATTERY_CHARGING(9),
        SCREEN_STATE(10),
        MOBILE_DATA_STATE(11),
        WIFI_STATE(12),
        METHOD_COVERAGE(13),
        ELAPSED_TIME (14),
        ENERGY_CONSUMED(15),
        BATTERY_POWER_DELTA(16),
        BATTERY_POWER(17),
        OTHER(-1);

        private int code;

        TestMetricCategory(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static TestMetricCategory inferMetricCategoryCode(String metricId){
            String metricName = metricId.toLowerCase();
            if (metricName.contains("cpu") && metricName.contains("load")){
                return CPU_LOAD;
            }
            else if (metricName.contains("cpu") && metricName.contains("frequency")){
                return CPU_FREQUENCY;
            }
            else if (metricName.contains("gpu") && metricName.contains("load")){
                return GPU_LOAD;
            }
            else if (metricName.contains("gpu") && metricName.contains("frequency")){
                return GPU_FREQUENCY;
            }
            else if (metricName.contains("gps") && metricName.contains("state")){
                return GPS_STATE;
            }
            else if (metricName.contains("bluetooth") && metricName.contains("state")){
                return BLUETOOTH_STATE;
            }
            else if (metricName.contains("memory")){
                return MEMORY;
            }
            else if (metricName.contains("rssi") && metricName.contains("level")){
                return WIFI_RSSI_LEVEL;
            }
            else if (metricName.contains("battery") && metricName.contains("status")){
                return BATTERY_STATUS;
            }
            else if (metricName.contains("battery") && metricName.contains("charging")){
                return BATTERY_CHARGING;
            }
            else if (metricName.contains("screen") && metricName.contains("state")){
                return SCREEN_STATE;
            }
            else if (metricName.contains("mobile") && metricName.contains("data") && metricName.contains("state")){
                return MOBILE_DATA_STATE;
            }
            else if (metricName.contains("wi-fi") && metricName.contains("state")){
                return WIFI_STATE;
            }
            else if (metricName.contains("coverage") && metricName.contains("method")){
                return METHOD_COVERAGE;
            }
            else if (metricName.contains("time") ){
                return ELAPSED_TIME;
            }
            else if (metricName.contains("energy") ){
                return ENERGY_CONSUMED;
            }
            else if (metricName.contains("power") && metricName.contains("delta") ){
                return BATTERY_POWER_DELTA;
            }
            else if (metricName.contains("power") && metricName.contains("battery") ){
                return BATTERY_POWER;
            }
            else return OTHER;
        }

        public static Metric getMetricType(TestMetricCategory testMetricCategory){
            Set<MetricValidator> s = testMetricValidations.get(testMetricCategory);
            if ( ! (s.contains(MetricValidator.NOMINAL) || s.contains(MetricValidator.OTHER))){
                return new NumericMutableMetric();
            }
            else return new NominalMutableMetric();
        }

   }


}

