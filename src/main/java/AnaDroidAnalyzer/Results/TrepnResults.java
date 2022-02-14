package AnaDroidAnalyzer.Results;



import AnaDroidAnalyzer.Results.Metrics.*;
import AnaDroidAnalyzer.Utils.Pair;
import org.json.simple.JSONObject;

import java.util.*;

public class TrepnResults {


    public int time = 0;
    public double totalEnergyConsumption = 0;

    public int startTime = -1;
    public int stopTime= -1;

    public boolean hasStartTime(){
        return this.startTime !=-1;
    }

    public boolean hasEndTime(){
        return this.stopTime !=-1;
    }

    public static Double getClosestPower (Map<Integer,Number> timeConsumption, int time){
        // calcular a medida de bateria mais aproximada
        int closestStart = 1000000, closestStop = 1000000;
        int difStart = 1000000, diffEnd = 1000000;
        int alternativeEnd = 0, alternativeStart=0;
        for (Number ii : timeConsumption.keySet()) {
            Integer i = ii.intValue();
            if(Math.abs(time-i) < difStart){
                difStart = Math.abs(time-i);
                alternativeStart =closestStart;
                closestStart = i;
            }
        }
        return timeConsumption.get(closestStart).doubleValue();
    }



    private double getClosestTimeAfter(double  time, TreeMap<Integer, Number>  samples){
        return samples.keySet().stream().filter( x -> x >= time).sorted().findFirst().get();
    }

    private double getClosestTimeBefore(double  time, TreeMap<Integer, Number>  samples){
        return samples.keySet().stream().filter( x -> x <= time).sorted(Comparator.reverseOrder()).findFirst().get();
    }

    private static double interpolate(double y0, double y1, double x, double x0, double x1 ){
        return y0 + ( ((x-x0)/(x1-x0) ) * ( y1-y0 ) );
    }

    public  double getTotalConsumptionInterpolated(TreeMap<Integer, Number> powerSamples, TreeMap<Integer,Number> stateSamples){
        double sum_energy_consumed = 0.0;
        double last_non_zero_power_registered= 0.0;
        long zeroPowerCount = powerSamples.values().stream().filter(x -> x.intValue()==0).count();
        System.out.println("Total of 0's in power samples -> " + zeroPowerCount + ". Percentage: " +( ((double)(zeroPowerCount/ (double)powerSamples.size()) ) * 100)+" %");
        Map<Integer,Number> desiredStateSamples = stateSamples.subMap(this.startTime, this.stopTime);
        double lastInterpolatedValue = 0.0, interpolatedPowerValue = 0.0;
        Optional<Integer> lastTime =desiredStateSamples.keySet().stream().findFirst();
        int lastTimeValue = lastTime.isPresent() ? lastTime.get() : 0;
        for (Integer key : desiredStateSamples.keySet()){
            Double x0 = getClosestTimeBefore(key, powerSamples);
            Double x1 = getClosestTimeAfter(key, powerSamples);
            if (Double.compare(x0,x1)==0){
                // i have an exact measurement
                interpolatedPowerValue = ((double) (powerSamples.get(x0.intValue()).doubleValue() / 1000000.0 ));
            }
            else {
                double y0 = powerSamples.get(x0.intValue()).doubleValue();
                double y1= powerSamples.get(x1.intValue()).doubleValue();

                if (y0 <=0){
                    // get last non zero value
                    y0 = last_non_zero_power_registered;
                }
                else {
                    if (y0 > last_non_zero_power_registered){
                        last_non_zero_power_registered = y0;
                    }
                }
                if (y1<=0){
                    y1 = last_non_zero_power_registered;
                }
                else {
                    if (y1 > last_non_zero_power_registered){
                        last_non_zero_power_registered = y1;
                    }
                }
                interpolatedPowerValue = ((double) ((double) (interpolate(y0,y1, key, x0,x1 )) / 1000000.0 ));
            }
            double avgInterp = ( lastInterpolatedValue + interpolatedPowerValue )/2;
            double elapsed_time_seconds = ((double) ((key - lastTimeValue) ) / 1000);
            sum_energy_consumed+= (avgInterp * elapsed_time_seconds );
            lastInterpolatedValue = interpolatedPowerValue;
            lastTimeValue = key;
        }
        this.time = stopTime-startTime;
        this.totalEnergyConsumption = sum_energy_consumed;
        return sum_energy_consumed;
    }


   /* public double getTotalConsumption(TreeMap<Integer,Number> powerSamples){
        int timeEnd = stopTime;
        int timeStart = startTime;
        // calcular a medida de bateria mais aproximada
        int closestStart = 1000000, closestStop = 1000000;
        int difStart = 1000000, diffEnd = 1000000;
        int alternativeEnd = 0, alternativeStart = 0;
        for (Number ii : powerSamples.keySet()) {
             Integer i = ii.intValue();
            if (Math.abs(timeStart - i) <= difStart) {
                difStart = Math.abs(timeStart - i);
                alternativeStart = closestStart;
                closestStart = i;
            }
            if (Math.abs(timeEnd - i) < diffEnd) {
                if (i <= timeEnd) {
                    closestStop = i;
                    diffEnd = Math.abs(timeEnd - i);
                } else {
                    alternativeEnd = i;
                }
            }
        }
        double startConsum = ((TreeMap <Integer,Number> ) powerSamples).firstEntry().getValue().doubleValue();
        double stopConsum = ((TreeMap <Integer,Number> ) powerSamples).lastEntry().getValue().doubleValue();
        double total = stopConsum;
        int totaltime = timeEnd  - timeStart;
        // if is a relevant test (in terms of time)
        if (total <= 0 && alternativeEnd != 0 ) {
            stopConsum = powerSamples.get(alternativeEnd).doubleValue();
//                 total = stopConsum-startConsum;.csv
            total = startConsum;
            totaltime = timeEnd - timeStart;
        }
        if (total <= 0 && alternativeStart != 0) {
            startConsum = powerSamples.get(alternativeStart).doubleValue();
//                total = stopConsum-startConsum;
            total = startConsum;
            totaltime = timeEnd - timeStart;
        }
        if (totaltime==0)
            return 0;
        int totalconsum = 0;
        double delta_seconds = 0, watt = 0;
        int toma = 0, delta = 0, ultimo = ((TreeMap<Integer, Number>) powerSamples).firstKey().intValue();
        for (Number ii : powerSamples.keySet()) {
            Integer i = ii.intValue();
            if (i>timeStart && i <= stopTime){
                delta = i-ultimo;
                delta_seconds = ((double) delta / ((double) 1000));
                double closestPowerSample = getClosestPower(powerSamples, i);
                watt = (closestPowerSample) / ((double) 1000000);
                //totalconsum+= (delta * (closestMemMeasure(timeConsumption,toma)));
                totalconsum += (delta_seconds * watt);

            }
            ultimo = i;
        }
        //double watt = (double) total/((double) 1000000);
        this.time=  totaltime;
        this.totalConsumption =  (((double) totaltime / ((double) 1000))) * (watt);
       return ((double) (((double) totaltime / ((double) 1000))) * (watt));
    }
*/

    public boolean bluetoothUsed (Map<Integer,Number> bluetoothStateSamples){
        for (Number i :bluetoothStateSamples.values()){
            if (i.intValue()>0) {
                return true;
            }
        }
        return false;
    }


   /* public void addCpuFreqSample ( Integer cpuNr, Integer time, Integer freq ) {
        if (this.cpuFrequencySamples.containsKey(cpuNr)){
            this.cpuFrequencySamples.get(cpuNr).put(time,freq );
        }
        else {
            HashMap<Number,Number> h = new HashMap<>();
            h.put(time,freq);
            this.cpuFrequencySamples.put(cpuNr,h);
        }
    }

    public void addCpuLoadSample ( Integer cpuNr, Integer time, Integer load ) {
        if (this.cpuLoadSamples.containsKey(cpuNr)){
            this.cpuLoadSamples.get(cpuNr).put(time,load );
        }
        else {
            HashMap<Number,Number> h = new HashMap<>();
            h.put(time,load);
            this.cpuLoadSamples.put(cpuNr,h);
        }
    }

    public Pair<Double,Double> getAvgAndTopMemory(){
        int total = 0, top = 0;
        for (Number me: this.memorySamples.values()) {
            Integer mem = me.intValue();
            total += mem;
            top = top > mem ? top : mem;
        }
        return new Pair<>((double) total / (double) memorySamples.size(),(double) top);
    }

    public  Pair<Number,Number> getDrainedBattery(){
        if (! this.batteryRemainingSamples.isEmpty()){
            return  new Pair<>(((TreeMap<Number, Number>) this.batteryRemainingSamples).firstEntry().getValue(),
                    ((TreeMap<Number, Number>) this.batteryRemainingSamples).firstEntry().getValue().intValue() - (((TreeMap<Number, Number>) this.batteryRemainingSamples).lastEntry().getValue()).intValue());
        }
        else{
            return new Pair<>(0,0);
        }

    }*/


    public double getAvgRSSILevel( Map<Integer,Number> rSSILevelSamples){
        int total = 0;
        for (Number mem: rSSILevelSamples.values()) {
            total += mem.intValue();
        }
        return ((double) total / (double) rSSILevelSamples.size());
    }


    public double getAvgGPULoad(Map<Integer,Number> gpuLoadSamples){
        int total = 0;
        for (Number mem: gpuLoadSamples.values()) {
            total += mem.intValue();
        }
        return ((double) total / (double) gpuLoadSamples.size());
    }

    public Pair<Double,Double> getAvgAndTopPower(Map<Integer,Number> powerSamples){
        double total = 0, top = 0;
        for (Number pow: powerSamples.values()) {
            total += pow.doubleValue();
            top = top > pow.doubleValue() ? top : pow.doubleValue();
        }
        return new Pair<>((double) total / (double) powerSamples.size(),(double) top);
    }

    public Pair<Integer,Integer> getBottomAndTopScreenBrigthness(Map<Integer,Number> screenBrigthtnessSamples){
        int bottom = 0, top = 0;
        for (Number me: screenBrigthtnessSamples.values()) {
            Integer mem = me.intValue();
            bottom  = mem < bottom ? mem : bottom;
            top = top > mem ? top : mem;
        }
        return new Pair<>(bottom, top);
    }

    public Pair<Double,Double> getAvgAndTopCPULoad( Map<Integer,Number> cpuLoadSamples){
        double total = 0, top = 0;
        int samples = cpuLoadSamples.size();
        for (Number m : cpuLoadSamples.values()) {
            total += m.doubleValue();
            top = top > m.doubleValue() ? top : m.doubleValue();
        }
        return new Pair<>((double) total / (double)samples,(double) top);
    }

    public Pair<Double,Double> getAvgAndTopCPUFreq(Map<Integer,Number> cpuFrequencySamples){
        double total = 0, top = 0;
        int samples = cpuFrequencySamples.size();
        for (Number m : cpuFrequencySamples.values()) {
            total += m.doubleValue();
            top = top > m.doubleValue() ? top : m.doubleValue();
        }
        return new Pair<>((double) total / (double)samples,(double) top);
    }

    public Pair<Double,Double> getAvgAndTopGPUFreq(Map<Integer,Number> gpuFrequencySamples){
        double total = 0, top = 0;
        for (Number valu : gpuFrequencySamples.values()) {
            Double value = valu.doubleValue();
            total += value;
            top = top > value ? top : value;

        }
        return new Pair<>((double) total / (double)(gpuFrequencySamples.size()),(double) top);
    }

    public boolean wifiUsed (Map<Integer,Number> wifiStateSamples){

        for (Number i :wifiStateSamples.values()){
            if (i.intValue()>0) {
                return true;
            }
        }
        return false;
    }

    public boolean mobileDataUsed (Map<Integer,Number> mobileDataStateSamples){

        for (Number i :mobileDataStateSamples.values()){
            if (i.intValue()>0) {
                return true;
            }
        }
        return false;
    }

    public boolean batteryCharging (Map<Integer,Number>  batteryStatusSamples){
        for (Number i :batteryStatusSamples.values()){
            if (i.intValue()>0) {
                return true;
            }
        }
        return false;
    }


    public boolean gpsUsed (Map<Integer,Number>  gpsSamples){
        for (Number i :gpsSamples.values()){
            if (i.intValue()>0) {
                return true;
            }
        }
        return false;
    }

    public boolean screenUsed (Map<Integer,Number> screenStateSamples){
        for (Number i : screenStateSamples.values()){
            if (i.intValue()>0) {
                return true;
            }
        }
        return false;
    }

    private String trimMetricID( String metricID){
        if (metricID.toLowerCase().contains("normalized")){
            return metricID.replaceAll("\\s","").replaceAll("\\[.*\\]", "").replace("(","").replace(")","");
        }
        else if (metricID.toLowerCase().contains("delta")){
            return metricID.replaceAll("\\s","").replaceAll("\\[.*\\]","").replace("(","").replace(")","").replace("*","");
        }
        else {
            return metricID.replaceAll("\\s","").replaceAll("\\(.*\\)", "").replaceAll("\\[.*\\]", "").replace("*","").replace("-","");
        }
    }

    public Set<IGreenSourceFormat> getAllGSMetrics( double totalcoverage,Set<TrepnCSVMetric> metricSet, JSONObject begin_state, JSONObject end_state){
        Set<IGreenSourceFormat> set = new HashSet<>();
        for (TrepnCSVMetric tcm : metricSet){
            Metric.TestMetricCategory testMetricCategory = Metric.TestMetricCategory.inferMetricCategoryCode(tcm.metricId);
            if (Metric.TestMetricCategory.getMetricType(testMetricCategory) instanceof NumericMutableMetric){
                set.add(new NumericMutableMetric(trimMetricID(tcm.metricId), tcm.metricUnit, tcm.getSamplesMap().subMap(this.startTime,this.stopTime)));
            }
            else if (Metric.TestMetricCategory.getMetricType(testMetricCategory) instanceof NominalMutableMetric){
                set.add(new NominalMutableMetric(trimMetricID(tcm.metricId), tcm.metricUnit));
            }
        }
        set.add( new NumericImmutableMetric("coverage","%", totalcoverage ));
        set.add( new NumericImmutableMetric("elapsedtime","ms", time ));
        set.add( new NumericImmutableMetric("energyconsumed","J", totalEnergyConsumption ));
        set.addAll(getMetricsFromDeviceResourcesState(begin_state,false));
        set.addAll(getMetricsFromDeviceResourcesState(end_state,true));
        
        return set;
    }

    private Collection<? extends IGreenSourceFormat> getMetricsFromDeviceResourcesState(JSONObject state, boolean isEnd) {
        Set<IGreenSourceFormat> set = new HashSet<>();
        if (isEnd){
            set.add( new NominalImmutableMetric("end_used_cpu","%", state.get("used_cpu").toString() ));
            set.add( new NominalImmutableMetric("end_used_mem_pss","KB", state.get("used_mem_pss").toString() ));
            set.add( new NominalImmutableMetric("end_used_mem_kernel","KB", state.get("used_mem_kernel").toString() ));
            set.add( new NominalImmutableMetric("end_nr_procceses","", state.get("nr_processes").toString() ));
            set.add( new NominalImmutableMetric("end_ischarging","b", state.get("ischarging").toString() ));
            set.add( new NominalImmutableMetric("end_battery_level","%", state.get("battery_level").toString() ));
            set.add( new NominalImmutableMetric("end_battery_temperature","dC",  state.get("battery_temperature").toString() ));
            set.add( new NominalImmutableMetric("end_battery_voltage","kV",  state.get("battery_voltage").toString() ));
            set.add( new NominalImmutableMetric("end_keyboard","b",  state.get("keyboard").toString() ));
            if (state.containsKey("nr_files_keyboard_folder")){
                set.add( new NominalImmutableMetric("end_nr_files_keyboard_folder","int",  state.get("nr_files_keyboard_folder").toString() ));
            }
            if (state.containsKey("main_cpu_freq")){
                set.add( new NominalImmutableMetric("end_main_cpu_freq","mHz",  state.get("main_cpu_freq").toString() ));
            }

        }
        else {
            set.add( new NominalImmutableMetric("begin_used_cpu","%", state.get("used_cpu").toString() ));
            set.add( new NominalImmutableMetric("begin_used_mem_pss","kB", state.get("used_mem_pss").toString() ));
            set.add( new NominalImmutableMetric("begin_used_mem_kernel","kB", state.get("used_mem_kernel").toString() ));
            set.add( new NominalImmutableMetric("begin_nr_procceses","", state.get("nr_processes").toString() ));
            set.add( new NominalImmutableMetric("begin_ischarging","b", state.get("ischarging").toString() ));
            set.add( new NominalImmutableMetric("begin_battery_level","%", state.get("battery_level").toString() ));
            set.add( new NominalImmutableMetric("begin_battery_temperature","dC", state.get("battery_temperature").toString() ));
            set.add( new NominalImmutableMetric("begin_battery_voltage","KV", state.get("battery_voltage").toString() ));
            set.add( new NominalImmutableMetric("begin_keyboard","", state.get("keyboard").toString() ));

            if (state.containsKey("nr_files_keyboard_folder")){
                set.add( new NominalImmutableMetric("begin_nr_files_keyboard_folder","int",  state.get("nr_files_keyboard_folder").toString() ));
            }
            if (state.containsKey("main_cpu_freq")){
                set.add( new NominalImmutableMetric("begin_main_cpu_freq","mHz",  state.get("main_cpu_freq").toString() ));
            }


        }
        return set;
    }


}