package AnaDroidAnalyzer.Utils;


import AnaDroidAnalyzer.GreenSourceBridge.GreenSourceAPI;
import AnaDroidAnalyzer.Results.Metrics.Issue;
import AnaDroidAnalyzer.Results.Metrics.TrepnCSVMetric;
import AndroidProjectRepresentation.*;



import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import org.json.simple.JSONObject;

import org.w3c.dom.Document;

import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.*;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;



/**
 * Created by rrua on 03/07/17.
 */
public class Utils {

    // Time  [ms]	Battery Remaining (%) [%]	Time  [ms]	Battery Status	Time  [ms]	Screen Brightness	Time  [ms]	Battery Power* [uW] (Raw)	Battery Power* [uW] (Delta)	Time  [ms]	GPU Frequency [KHz]	Time  [ms]	GPU Load [%]	Time  [ms]	CPU1 Frequency [kHz]	Time  [ms]	CPU2 Frequency [kHz]	Time  [ms]	CPU3 Frequency [kHz]	Time  [ms]	CPU4 Frequency [kHz]	Time  [ms]	CPU1 Load [%]	Time  [ms]	CPU2 Load [%] Time  [ms]	CPU3 Load [%]	Time  [ms]	CPU4 Load [%]	Time  [ms]	Application State	Description


    public static final String timeNormal = "Time.*";
    public static final String batteryPower = "Battery\\ Power.*";
    public static final String batteryPowerDelta = "Battery\\ Power.*Delta.*";
    public static final String batteryStatus = "Battery\\ Status.*";
    public static final String stateInt = "Application\\ State.*";
    public static final String stateDescription = "Description.*";
    public static final String batteryRemaining = "Battery\\ Remaining.*";
    public static final String screenbrightness = "Screen\\ Brightness.*";
    public static final String gpufreq = "GPU\\ Frequency*";
    public static final String gpuLoad = "GPU\\ Load.*";
    public static final String cp1freq = "CPU1\\ Frequency.*";
    public static final String cp2freq = "CPU2\\ Frequency.*";
    public static final String cp3freq = "CPU3\\ Frequency.*";
    public static final String cp4freq = "CPU4\\ Frequency.*";
    public static final String cp5freq = "CPU5\\ Frequency.*";
    public static final String cp6freq = "CPU6\\ Frequency.*";
    public static final String cp7freq = "CPU7\\ Frequency.*";
    public static final String cpu8freq = "CPU8\\ Frequency.*";
    public static final String cpu1Load = "CPU1\\ Load.*";
    public static final String cpu2Load = "CPU2\\ Load.*";
    public static final String cpu3Load = "CPU3\\ Load.*";
    public static final String cpu4Load = "CPU4\\ Load.*";
    public static final String cpu5Load = "CPU5\\ Load.*";
    public static final String cpu6Load = "CPU6\\ Load.*";
    public static final String cpu7Load = "CPU7\\ Load.*";
    public static final String cpu8Load = "CPU8\\ Load.*";
    public static final String cpuLoad = "CPU\\ Load.*";
    public static final String cpuLoadNormalized = "CPU\\ Load.*Normalized.*";
    public static final String memory = "Memory\\ Usage.*";
    public static final String mobileData = "Mobile.*";
    public static final String wifiState = "Wi-Fi\\ State.*";
    public static final String wifiRSSILevel = "Wi-Fi\\ RSSI.*";
    public static final String screenState = "Screen\\ State.*";
    public static final String bluetoothState = "Bluetooth\\ State.*";
    public static final String gpsState = "GPS\\ State.*";




    public static Pair<Integer, Integer> getMatch(HashMap<String, Pair<Integer, Integer>> hashMap, String s) {
        for (String st : hashMap.keySet()) {
            if (st.matches(s))
                return hashMap.get(st);
        }
        return null;
    }

    // pair tempo -> coluna
    public static Set<TrepnCSVMetric>fetchColumns(List<String[]> resolvedData) {
       // HashMap<String, Pair<Integer, Integer>> hashMap = new HashMap<String, Pair<Integer, Integer>>(); //Column name -> (TimeColumn,valueColumnn)
        Set<TrepnCSVMetric> metricSet = new HashSet<>(); //  metricid ->
        String[] row = new String[100];
        for (int i = 0; i < resolvedData.size(); i++) {
            row = resolvedData.get(i);
            if (row.length <= 0 || row[0] == null) continue;
            if (row[0].matches("Time.*")) {// find header of results table
                for (int column_index = 0; column_index < row.length; column_index++) {
                    if (row[column_index] == null){
                        continue;
                    }
                    if (row[column_index].matches("Time.*")) { // tou na linha do cabeçalho da tabela
                        String met_id = row[column_index+1];
                        TrepnCSVMetric tcv = new TrepnCSVMetric(met_id);
                        tcv.setTime_csv_column(column_index);
                        tcv.setValue_csv_column(column_index+1);
                        metricSet.add(tcv);
                        //hashMap.put(row[column_index + 1], new Pair<Integer, Integer>(column_index, column_index + 1));
                    } else if (row[column_index].matches(stateDescription + ".*")) {
                        String met_id = row[column_index];
                        TrepnCSVMetric tcv = new TrepnCSVMetric(met_id);
                        tcv.setTime_csv_column(column_index-2);
                        tcv.setValue_csv_column(column_index);
                        metricSet.add(tcv);
                        //hashMap.put(row[column_index], new Pair<Integer, Integer>(column_index - 2, column_index));
                    } else if (row[column_index].matches(batteryPowerDelta + ".*")) {
                        String met_id = row[column_index];
                        TrepnCSVMetric tcv = new TrepnCSVMetric(met_id);
                        tcv.setTime_csv_column(column_index-2);
                        tcv.setValue_csv_column(column_index);
                        metricSet.add(tcv);
                        //hashMap.put(row[column_index], new Pair<Integer, Integer>(column_index - 2, column_index));
                    }
                }
                break;
            }
        }
        return metricSet;
    }


    // XML UTILS

    public static List<Issue> parseLintResulsXML(String file) {
        List<Issue> list = new ArrayList<Issue>();
        try {
            File fXmlFile = new File(file);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            doc.getDocumentElement().normalize();
            NodeList issueList = doc.getDocumentElement().getElementsByTagName("issue");
            for (int temp = 0; temp < issueList.getLength(); temp++) {
                Issue issue = new Issue("-", issueList.item(temp).getAttributes().getNamedItem("id").getNodeValue());
                issue.message = issueList.item(temp).getAttributes().getNamedItem("message").getNodeValue();
                issue.severity = issueList.item(temp).getAttributes().getNamedItem("severity").getNodeValue();
                issue.priority = Integer.valueOf(issueList.item(temp).getAttributes().getNamedItem("priority").getNodeValue());
                issue.summary = issueList.item(temp).getAttributes().getNamedItem("summary").getNodeValue();
                for (int temp2 = 0; temp2 < issueList.item(temp).getChildNodes().getLength(); temp2++) {
                    if (issueList.item(temp).getChildNodes().item(temp2).getAttributes() != null) {
                        issue.file = issueList.item(temp).getChildNodes().item(temp2).getAttributes().getNamedItem("file").getNodeValue();
                        issue.line = Integer.valueOf(issueList.item(temp).getChildNodes().item(temp2).getAttributes().getNamedItem("line").getNodeValue());
                    }
                }
                list.add(issue);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

//        for (Issue i : list)
//            System.out.println(i.id +" " + i.line  +"  " + i.file);
        return list;
    }

    public  JSONArray parseAndroidApis() {
        JSONArray ja = new JSONArray();
        ClassLoader classLoader = getClass().getClassLoader();
        File f = new File(classLoader.getResource("patterns_length1.csv").getFile());
        //File f = new File("/Users/ruirua/Downloads/patterns_length1.csv");
        CsvParserSettings settings = new CsvParserSettings();
        settings.setMaxCharsPerColumn(25000);
        settings.getFormat().setLineSeparator("\r");
        settings.getFormat().setDelimiter(';');
        CsvParser parser = new CsvParser(settings);
        String[] row = null;
        // 3rd, parses all rows of data in selected columns from the CSV file into a matrix
        List<String[]> resolvedData = null;
        try {
            resolvedData = parser.parseAll(new FileReader(f.getAbsolutePath()));
        } catch (FileNotFoundException e) {
            System.out.println("[ANALYZER]: File Not Found: There is no  csv file in directory! to generate results");

        }
        for (int i = 1; i < resolvedData.size(); i++) {

            row = resolvedData.get(i);
            if (row.length > 1) {
                JSONObject jo = new JSONObject();
                jo.put("category", row[1]);
                String s = row[0].replaceAll("\\(.*?\\)", "");
                s = s.replaceAll("-", "");
                String[] fullMethodDefinition = s.split("\\.");
                String methodName = fullMethodDefinition[fullMethodDefinition.length > 0 ? fullMethodDefinition.length - 1 : 0];
                jo.put("methodName", methodName);
                jo.put("fullMethodDefinition", s);
                ja.add(jo);


            }
        }
        try (FileWriter file = new FileWriter("redAPIS.json")) {

            file.write(ja.toJSONString());
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return ja;
    }


    public JSONArray decodeAndroidAPIS() {
        JSONParser parser = new JSONParser();
        JSONArray ja = new JSONArray();
        try {
            Object obj = parser.parse(new FileReader("redAPIS.json"));
            JSONArray jsonObject = (JSONArray) obj;
            JSONArray msg = (JSONArray) jsonObject;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return ja;
    }




    public static JSONObject getTest(String appid, String monkey, String tt ) {
        JSONObject test = new JSONObject();
        test.put("test_application", appid); //TODO
        test.put("test_tool", monkey); // TODO
        test.put("test_orientation",tt );
        return test;
    }

    public static JSONArray getAppMethodsAndMetrics(APICallUtil acu, GreenSourceAPI grr) {
        JSONArray ja = new JSONArray();
        for (ClassInfo ci : acu.proj.getCurrentApp().allJavaClasses) {
            for (MethodInfo mi : ci.classMethods.values()) {
                JSONObject jo = new JSONObject();
                jo.put("method_class", ci.getClassID());
                jo.put("method_name", mi.methodName);
                // hashArgs
                String args = "";
                for (Variable v : mi.args) {
                    args += v.arrayCount + v.type + v.varName;
                }

                jo.put("method_id", mi.getMethodID());
                jo.put("method_hash_args", args.hashCode());
                ja.add(jo);
                grr.methodMetrics.addAll(getMethodsMetrics(mi));
            }
        }
        return ja;
    }


     public static JSONObject getMethodAPIS(MethodInfo mi ){
         JSONObject jo = new JSONObject();
         jo.put("methodName",mi.methodName);
         JSONArray ja = new JSONArray();
         for (MethodOfAPI moa : mi.androidApi){
             JSONObject job = new JSONObject();
             job.put("class", moa.referenceClass );
             job.put("method", moa.method);
             ja.add(job);
         }
         jo.put("androidAPIS",ja);
         ja = new JSONArray();
         for (MethodOfAPI moa : mi.javaApi){
             JSONObject job = new JSONObject();
             job.put("class", moa.referenceClass );
             job.put("method", moa.method);
             ja.add(job);
         }
         jo.put("javaAPIS",ja);
         ja = new JSONArray();
         for (MethodOfAPI moa : mi.unknownApi){
             JSONObject job = new JSONObject();
             job.put("class", moa.referenceClass );
             job.put("method", moa.method);
             ja.add(job);
         }
         jo.put("unknownAPIS",ja);
         ja = new JSONArray();
         for (MethodOfAPI moa : mi.externalApi){
             JSONObject job = new JSONObject();
             job.put("class", moa.referenceClass );
             job.put("method", moa.method);
             ja.add(job);
         }
         jo.put("externalAPI",ja);
         return jo;
     }


    public static void writeJSONMethodAPIS (JSONArray ja, String file){

        try {
            writeFile(new File(file),ja.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeJSONObject (JSONObject ja, String file){

        try {
            writeFile(new File(file),ja.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeFile(File file, String content) throws IOException{
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(content);
        bw.flush();
        bw.close();
    }

    public static JSONArray getMethodsMetrics(MethodInfo mi) {
        JSONArray ja = new JSONArray();
        String args = "";
        for (Variable v : mi.args) {
            args += v.arrayCount + v.type + v.varName;
        }
        String idMethod =mi.getMethodID();
        JSONObject o = new JSONObject();
        o.put("mm_method", idMethod);
        o.put("mm_metric", "androidapi");
        o.put("mm_value", mi.androidApi.size());
        o.put("mm_coeficient", 1);
        ja.add(o);
        o.put("mm_method", idMethod);
        o.put("mm_metric", "cc");
        o.put("mm_value", mi.cyclomaticComplexity);
        o.put("mm_coeficient", 1);
        ja.add(o);
        o = new JSONObject();
        o.put("mm_method", idMethod);
        o.put("mm_metric", "loc");
        o.put("mm_value", mi.linesOfCode);
        o.put("mm_coeficient", 1);
        ja.add(o);
        o = new JSONObject();
        o.put("mm_method", idMethod);
        o.put("mm_metric", "androidapi");
        o.put("mm_value", mi.androidApi.size());
        o.put("mm_coeficient", 1);
        ja.add(o);
        o = new JSONObject();
        o.put("mm_method", idMethod);
        o.put("mm_metric", "javaapi");
        o.put("mm_value", mi.javaApi.size());
        o.put("mm_coeficient", 1);
        ja.add(o);
        o = new JSONObject();
        o.put("mm_method", idMethod);
        o.put("mm_metric", "unknownapi");
        o.put("mm_value", mi.externalApi.size() + mi.unknownApi.size());
        o.put("mm_coeficient", 1);
        ja.add(o);
        o = new JSONObject();
        o.put("mm_method", idMethod);
        o.put("mm_metric", "nrargs");
        o.put("mm_value", mi.args.size());
        o.put("mm_coeficient", 1);
        ja.add(o);
        o = new JSONObject();
        o.put("mm_method", idMethod);
        o.put("mm_metric", "isstatic");
        o.put("mm_value", mi.isStatic ? 1 : 0);
        o.put("mm_coeficient", 1);
        ja.add(o);
        return ja;
    }



    public static JSONArray getMethodsMetricsMethodOriented(MethodInfo mi, String time, String energy, String methodInvoked, Double [] testResults) {
        JSONArray ja = new JSONArray();
        String idMethod = GreenSourceAPI.generateMethodID(mi);
        JSONObject o = new JSONObject();
        o.put("mm_method", idMethod);
        o.put("mm_metric", "androidapis");
        o.put("mm_value", mi.androidApi.size());
        o.put("mm_coeficient", 1);
        ja.add(o);
        o.put("mm_method", idMethod);
        o.put("mm_metric", "cc");
        o.put("mm_value", mi.cyclomaticComplexity);
        o.put("mm_coeficient", 1);
        ja.add(o);
        o = new JSONObject();
        o.put("mm_method", idMethod);
        o.put("mm_metric", "loc");
        o.put("mm_value", mi.linesOfCode);
        o.put("mm_coeficient", 1);
        ja.add(o);
        o = new JSONObject();
        o.put("mm_method", idMethod);
        o.put("mm_metric", "androidapis");
        o.put("mm_value", mi.androidApi.size());
        o.put("mm_coeficient", 1);
        ja.add(o);
        o = new JSONObject();
        o.put("mm_method", idMethod);
        o.put("mm_metric", "javaapis");
        o.put("mm_value", mi.javaApi.size());
        o.put("mm_coeficient", 1);
        ja.add(o);
        o = new JSONObject();
        o.put("mm_method", idMethod);
        o.put("mm_metric", "unknownapis");
        o.put("mm_value", mi.externalApi.size() + mi.unknownApi.size());
        o.put("mm_coeficient", 1);
        ja.add(o);
        o = new JSONObject();
        o.put("mm_method", idMethod);
        o.put("mm_metric", "nrargs");
        o.put("mm_value", mi.args.size());
        o.put("mm_coeficient", 1);
        ja.add(o);
        o = new JSONObject();
        o.put("mm_method", idMethod);
        o.put("mm_metric", "isstatic");
        o.put("mm_value", mi.isStatic ? 1 : 0);
        o.put("mm_coeficient", 1);
        ja.add(o);


         // dynamic metrics
        o = new JSONObject();
        o.put("mm_method", idMethod);
        o.put("mm_metric", "wifistate");
        o.put("mm_value", testResults[0]);
        o.put("mm_coeficient", 1);
        o.put("mm_invokation", methodInvoked);
        ja.add(o);

        o = new JSONObject();
        o.put("mm_method", idMethod);
        o.put("mm_metric", "mobiledatastate");
        o.put("mm_value", testResults[1]);
        o.put("mm_coeficient", 1);
        o.put("mm_invokation", methodInvoked);
        ja.add(o);

        o = new JSONObject();
        o.put("mm_method", idMethod);
        o.put("mm_metric", "screenstate");
        o.put("mm_value", testResults[2]);
        o.put("mm_coeficient", 1);
        o.put("mm_invokation", methodInvoked);
        ja.add(o);

        o = new JSONObject();
        o.put("mm_method", idMethod);
        o.put("mm_metric", "batterystatus");
        o.put("mm_value", testResults[3]);
        o.put("mm_coeficient", 1);
        o.put("mm_invokation", methodInvoked);
        ja.add(o);

        o = new JSONObject();
        o.put("mm_method", idMethod);
        o.put("mm_metric", "wifirssilevel");
        o.put("mm_value", testResults[4]);
        o.put("mm_coeficient", 1);
        o.put("mm_invokation", methodInvoked);
        ja.add(o);

        o = new JSONObject();
        o.put("mm_method", idMethod);
        o.put("mm_metric", "memory");
        o.put("mm_value", testResults[5]);
        o.put("mm_coeficient", 1);
        o.put("mm_invokation", methodInvoked);
        ja.add(o);

        o = new JSONObject();
        o.put("mm_method", idMethod);
        o.put("mm_metric", "bluetoothstate");
        o.put("mm_value", testResults[6]);
        o.put("mm_coeficient", 1);
        o.put("mm_invokation", methodInvoked);
        ja.add(o);

        o = new JSONObject();
        o.put("mm_method", idMethod);
        o.put("mm_metric", "gpufrequency");
        o.put("mm_value", testResults[7]);
        o.put("mm_coeficient", 1);
        o.put("mm_invokation", methodInvoked);
        ja.add(o);

        o = new JSONObject();
        o.put("mm_method", idMethod);
        o.put("mm_metric", "cpuloadnormalized");
        o.put("mm_value", testResults[8]);
        o.put("mm_coeficient", 1);
        o.put("mm_invokation", methodInvoked);
        ja.add(o);

        o = new JSONObject();
        o.put("mm_method", idMethod);
        o.put("mm_metric", "gpsstate");
        o.put("mm_value", testResults[9]);
        o.put("mm_coeficient", 1);
        o.put("mm_invokation", methodInvoked);
        ja.add(o);

        //
        o = new JSONObject();
        o.put("mm_method", idMethod);
        o.put("mm_metric", "energy");
        o.put("mm_value", energy);
        o.put("mm_coeficient", 1);
        o.put("mm_invokation", methodInvoked);
        ja.add(o);
        o = new JSONObject();
        o.put("mm_method", idMethod);
        o.put("mm_metric", "time");
        o.put("mm_value", time);
        o.put("mm_coeficient", 1);
        o.put("mm_invokation", methodInvoked);
        ja.add(o);
        return ja;
    }


    public static JSONArray getMethodsInvoked(String testResultsID, Map<String,Integer> methodsSet) {
        JSONArray ja = new JSONArray();
        for (String s : methodsSet.keySet()) {
            JSONObject test = new JSONObject();
            test.put("test_results", testResultsID);
            test.put("method", s);
            test.put("times_invoked", methodsSet.get(s));
            ja.add(test);
        }
        //methodsSet.clear();
        return ja;
    }

    public static JSONObject getMethodsInvoked(String testResultsID, String  metID , String timesInvoked) {
        JSONObject javali = new JSONObject();
        javali.put("method", metID);
        javali.put("times_invoked", timesInvoked );
        javali.put("test_results",testResultsID  );
        return javali;
    }



    public static int convertMem(String toConvert){
        String onlyChars = toConvert.replaceAll("\\d", "");
        String onlyNrs = toConvert.replaceAll("[A-Za-z]", "");
        Integer i = Integer.parseInt(onlyNrs);
        if (onlyChars.matches("kB")){
            i = i * 1024;
        }
        else if (onlyChars.matches("MB")) {
            i = i* 1024*1024;
        }
        return i;
    }



    public static JSONObject getTestResult(String seed, String desc, String testiD, String profilerID, String deviceStateID, JSONObject device_init ) {
        JSONObject tesResults = new JSONObject();
        tesResults.put("test_results_seed", seed);
        tesResults.put("test_results_description", desc);
        tesResults.put("test_results_test", testiD);
        tesResults.put("test_results_profiler", profilerID);
        tesResults.put("test_results_device_state", deviceStateID);
        //tesResults.put("test_results_device_begin_state", deviceStartID);
        //tesResults.put("test_results_device_end_state", deviceEndID);
//        tesResults.put("test_init_mem", convertMem(((String) device_init.get("device_state_mem"))));
//        tesResults.put("test_init_cpu_free", device_init.get("device_state_cpu_free"));
//        tesResults.put("test_init_nr_processes_running", device_init.get("device_state_nr_processes_running"));
//        tesResults.put("test_end_mem", convertMem ((String) device_end.get("device_state_mem")));
//        tesResults.put("test_end_cpu_free", device_end.get("device_state_cpu_free"));
//        tesResults.put("test_end_nr_processes_running", device_end.get("device_state_nr_processes_running"));
        //tesResults.put("test_results_api_level", device_init.get("device_state_api_level"));
        //tesResults.put("test_results_android_version", device_init.get("device_state_android_version"));
        if (device_init.containsKey("timestamp")){
            tesResults.put("test_results_unix_timestamp", device_init.get("timestamp"));
        }
        else {
            tesResults.put("test_results_unix_timestamp",  String.valueOf((deviceStateID  + profilerID.hashCode()  + Double.hashCode( Double.parseDouble(((String) device_init.get("device_state_cpu_free"))) + Double.parseDouble(((String) device_init.get("device_state_mem")).replaceAll("(k|M|G|T)(b|B)", "")) +   Double.parseDouble(((String) device_init.get("device_state_nr_processes_running"))))).hashCode())   );
        }
        return tesResults;
    }

    public static JSONObject getTestMetricsMethodOriented(String testid, double coverage) {
        JSONObject testMetrics = new JSONObject();
        testMetrics.put("test_results", testid);
        testMetrics.put("metric", "coverage");
        testMetrics.put("value", coverage);
        testMetrics.put("coeficient", 1);
        return testMetrics;
    }




    public static JSONArray getClasses(Iterable<ClassInfo> classses){
        JSONArray ja = new JSONArray();
        for (ClassInfo ci : classses){
            JSONObject jo = new JSONObject();
            jo.put("class_id",  ci.classPackage + "." + ci.className); // TODO replace with ci.getClassID()
            jo.put("class_name", ci.className);
            jo.put("class_package", ci.classPackage);
           // jo.put("class_non_acc_mod", );
            jo.put("class_application", ci.classVariables.size());
           // jo.put("class_is_interface", ci.isInterface);
            jo.put("class_acc_mod", 1);
            if  (ci.extendedClass!=null)
                jo.put("class_superclass", ci.extendedClass);
            ja.add(jo);

        }
        return ja;
    }




    public static JSONArray getClassMetrics (String classId , ClassInfo ci ) {

        JSONArray ja = new JSONArray();
        JSONObject jo = new JSONObject();
        jo.put("cm_class", classId);
        jo.put("cm_metric", "numberOfVars");
        jo.put("cm_value", ci.classVariables.size());
        jo.put("cm_coeficient", 1);
        ja.add(jo);
        jo = new JSONObject();
        jo.put("cm_class", classId);
        jo.put("cm_metric", "numberOfMethods");
        jo.put("cm_value", ci.classMethods.size());
        jo.put("cm_coeficient", 1);
        ja.add(jo);
        return ja;


    }




    public static void write (Writer w, Iterable<String> l) throws IOException {

        boolean first = true;
        StringBuilder sb = new StringBuilder();
        for (String value: l)
        {
            if(!first){
                sb.append(",");
                sb.append(value);
            }
            else {
                sb.append(value);
                first=false;
            }
        }

        sb.append("\n");
        w.append(sb.toString());
    }

    public static void writeAndClose (Writer w, List<String> l) {

        boolean first = true;
        StringBuilder sb = new StringBuilder();
        for (String value: l)
        {
            if(!first){
                sb.append(",");
                sb.append(value);
            }
            else {
                sb.append(value);
                first=false;
            }
        }

        sb.append("\n");

        try {
            w.append(sb.toString());
            w.flush();
            w.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void appendToFile(FileWriter fw, List<String> l){
        try {
            write(fw,l);
            l.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static int getMemoryCoeficient(String def){
        int coef = 8;

        if( def.contains("k")|| def.contains("K")){
            coef*=(1024*1024);
        }
        if( def.contains("M")){
            coef*=1024;
        }
        if( def.contains("G")){
            coef*=(1024*1024*1024);
        }

        return coef;
    }



    public static JSONArray getTestMetrics(String testid, Map<String,String> res, double energy, double time, double coverage, JSONObject begin, JSONObject end) {
        JSONArray ja = new JSONArray();
        JSONObject testMetrics = new JSONObject();
        for (String metric : res.keySet()){
            if (  !metric.equals("testId") && ! metric.equals("testName")) {
                testMetrics.put("test_results", testid);
                testMetrics.put("metric", metric.toLowerCase());
                if (res.get(metric).equals("false")){
                    testMetrics.put("value", 0);
                }
                else if (res.get(metric).equals("true")){
                    testMetrics.put("value", 1);
                }
                else{
                    testMetrics.put("value", res.get(metric));
                }
                testMetrics.put("coeficient", 1);
                ja.add(testMetrics);
                testMetrics = new JSONObject();
            }
        }
        begin.remove("device_state_api_level");
        begin.remove("device_state_android_version");

        // begin
        testMetrics = new JSONObject();
        testMetrics.put("test_results", testid);
        testMetrics.put("metric", "test_init_cpu_free" );
        testMetrics.put("value",  begin.get("device_state_cpu_free"));
        testMetrics.put("coeficient", 1);
        ja.add(testMetrics);
        testMetrics = new JSONObject();
        testMetrics.put("test_results", testid);
        testMetrics.put("metric", "test_init_mem" );
        testMetrics.put("value", ((String) begin.get("device_state_mem")).replaceAll( "(k|M|G|T)(b|B)", ""));
        testMetrics.put("coeficient", getMemoryCoeficient(((String) begin.get("device_state_mem"))));
        ja.add(testMetrics);
        testMetrics = new JSONObject();
        testMetrics.put("test_results", testid);
        testMetrics.put("metric", "test_init_nr_processes_running" );
        testMetrics.put("value",  begin.get("device_state_nr_processes_running"));
        testMetrics.put("coeficient", 1);
        ja.add(testMetrics);
        // end
        testMetrics = new JSONObject();
        testMetrics.put("test_results", testid);
        testMetrics.put("metric", "test_end_cpu_free" );
        testMetrics.put("value",  end.get("device_state_cpu_free"));
        testMetrics.put("coeficient", 1);
        ja.add(testMetrics);
        testMetrics = new JSONObject();
        testMetrics.put("test_results", testid);
        testMetrics.put("metric", "test_end_mem" );
        testMetrics.put("value", ((String) end.get("device_state_mem")).replaceAll( "(k|M|G|T)(b|B)", ""));
        testMetrics.put("coeficient", getMemoryCoeficient(((String) end.get("device_state_mem"))));
        ja.add(testMetrics);
        testMetrics = new JSONObject();
        testMetrics.put("test_results", testid);
        testMetrics.put("metric", "test_end_nr_processes_running" );
        testMetrics.put("value",  end.get("device_state_nr_processes_running"));
        testMetrics.put("coeficient", 1);
        ja.add(testMetrics);

/*
        testMetrics.put("test_results", testid);
        testMetrics.put("metric", "wifistate");
        testMetrics.put("value", res[5]);
        testMetrics.put("coeficient", 1);
        ja.add(testMetrics);
        testMetrics = new JSONObject();
        testMetrics.put("test_results", testid);
        testMetrics.put("metric", "mobiledatastate");
        testMetrics.put("value", res[6]);
        testMetrics.put("coeficient", 1);
        ja.add(testMetrics);
        testMetrics = new JSONObject();
        testMetrics.put("test_results", testid);
        testMetrics.put("metric", "screenstate");
        testMetrics.put("value",res[7]);
        testMetrics.put("coeficient", 1);
        ja.add(testMetrics);
        testMetrics = new JSONObject();
        testMetrics.put("test_results", testid);
        testMetrics.put("metric", "batterystatus");
        testMetrics.put("value", res[8]);
        testMetrics.put("coeficient", 1);
        ja.add(testMetrics);
        testMetrics = new JSONObject();
        testMetrics.put("test_results", testid);
        testMetrics.put("metric", "wifirssilevel");
        testMetrics.put("value", res[9]);
        testMetrics.put("coeficient", 1);
        ja.add(testMetrics);
        testMetrics = new JSONObject();
        testMetrics.put("test_results", testid);
        testMetrics.put("metric", "avgmemory");
        testMetrics.put("value", res[10]);
        testMetrics.put("coeficient", 1);
        ja.add(testMetrics);
        testMetrics = new JSONObject();
        testMetrics.put("test_results", testid);
        testMetrics.put("metric", "maxmemory");
        testMetrics.put("value", res[11]);
        testMetrics.put("coeficient", 1);
        ja.add(testMetrics);
        testMetrics = new JSONObject();
        testMetrics.put("test_results", testid);
        testMetrics.put("metric", "bluetoothstate");
        testMetrics.put("value", res[12]);
        testMetrics.put("coeficient", 1);
        testMetrics = new JSONObject();
        testMetrics.put("test_results", testid);
        testMetrics.put("metric", "gpufrequency");
        testMetrics.put("value", res[13]);
        testMetrics.put("coeficient", 1);
        ja.add(testMetrics);
        testMetrics = new JSONObject();
        testMetrics.put("test_results", testid);
        testMetrics.put("metric", "avgcpuload");
        testMetrics.put("value", res[14]);
        testMetrics.put("coeficient", 1);
        ja.add(testMetrics);
        testMetrics = new JSONObject();
        testMetrics.put("test_results", testid);
        testMetrics.put("metric", "maxcpuload");
        testMetrics.put("value", res[15]);
        testMetrics.put("coeficient", 1);
        ja.add(testMetrics);
        testMetrics = new JSONObject();
        testMetrics.put("test_results", testid);
        testMetrics.put("metric", "gpsstate");
        testMetrics.put("value", res[16]);
        testMetrics.put("coeficient", 1);
        ja.add(testMetrics);
        testMetrics = new JSONObject();
        testMetrics.put("test_results", testid);
        testMetrics.put("metric", "energy");
        testMetrics.put("value", energy);
        testMetrics.put("coeficient", 1);
        ja.add(testMetrics);
        testMetrics = new JSONObject();
        testMetrics.put("test_results", testid);
        testMetrics.put("metric", "time");
        testMetrics.put("value", time);
        testMetrics.put("coeficient", 1);
        ja.add(testMetrics);
        testMetrics = new JSONObject();
        testMetrics.put("test_results", testid);
        testMetrics.put("metric", "coverage");
        testMetrics.put("value", coverage);
        testMetrics.put("coeficient", 1);
        ja.add(testMetrics);
        */

        return ja;


    }


    public static JSONObject loadJSONObj(String filePath){
        JSONParser parser = new JSONParser();
        JSONObject obj1 = new JSONObject();
        try {
            Object obj = parser.parse(new FileReader(filePath));
            obj1 = (JSONObject) obj;
            return obj1;
        }
        catch (ClassCastException e) {
            return  loadJSONArrayAsJSONObj(filePath);
        }

        catch (Exception e) {
            e.printStackTrace();
        }

        return new JSONObject();
    }

    public static JSONObject loadJSONArrayAsJSONObj(String filePath){
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader(filePath));
            JSONArray jas = ((JSONArray) obj);
            JSONObject jo = new JSONObject();
            jas.forEach(x -> jo.put(((JSONObject) x).get("name"),x ) );
            return jo;
        } catch (FileNotFoundException e) {

        }
        catch (Exception e) {

        }
        return new JSONObject();
    }



    public static JSONArray loadJSONArray(String filePath){
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader(filePath));
            return (JSONArray)obj;
        } catch (FileNotFoundException e) {

        }
        catch (Exception e) {

        }
        return new JSONArray();
    }




}