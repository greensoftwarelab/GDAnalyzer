package AnaDroidAnalyzer.Results;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestResults extends TrepnResults {


    public Map <String, Integer >  invokedMethods = new HashMap<>();
    public String testId = "";
    public String testName = "";
    public  JSONObject methods_invoked_specs_and_metrics = new JSONObject();
    public  JSONObject test_specs_and_metrics = new JSONObject();



    public TestResults(){
        super();
        /*
        headers.add("Test Number");
        headers.add("Test Name");
        headers.add("Consumption (J)");
        headers.add("Time (ms)"); headers.add("Method coverage (%)");
        headers.add("Wifi?");
        headers.add("Mobile Data?");
        headers.add("Screen state");
        headers.add("Battery Charging?");
        headers.add("Avg RSSI Level");
        headers.add("Avg Mem Usage");
        headers.add("Top Mem Usage");
        headers.add("Bluetooth?");
        headers.add("Avg gpu Load");
        headers.add("Avg CPU Load");
        headers.add("Max CPU Load");
        headers.add("GPS?");
        metricsName.add("testId");
        metricsName.add("testName");
        metricsName.add("totalEnergy");
        metricsName.add("Time");
        metricsName.add("Coverage");
        metricsName.add("WifiState");
        metricsName.add("MobileDataState");
        metricsName.add("ScreenState");
        metricsName.add("batteryCharging");
        metricsName.add("BatteryStatus");
        metricsName.add("WifiRSSILevel");
        metricsName.add("AvgMemory");
        metricsName.add("TopMemory");
        metricsName.add("BluetoothState");
        metricsName.add("AvgGPUFrequency");
        metricsName.add("topGPUFrequency");
        metricsName.add("AvgGPULoad");
        metricsName.add("GpsState");
        metricsName.add("AvgCPULoad");
        metricsName.add("TopCPULoad");

         */

    }


    public double getCoverage(int len){
        return (double) (invokedMethods.keySet().size() / len);
    }


    public JSONObject toAnalysisJSONFile() {

        return new JSONObject();

    }
}

