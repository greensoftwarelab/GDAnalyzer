package AnaDroidAnalyzer.Analyzer;

import AnaDroidAnalyzer.GreenSourceBridge.GreenSourceAPI;
import AnaDroidAnalyzer.Results.Metrics.IGreenSourceFormat;
import AnaDroidAnalyzer.Results.Metrics.TrepnCSVMetric;
import AnaDroidAnalyzer.Results.TrepnResults;
import AnaDroidAnalyzer.Results.TestResults;
import AnaDroidAnalyzer.Utils.Utils;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestOrientedAnalyzer extends MainAnalyzer { //implements PowerLogAnalyzer {

    public static final String stopTag="stopped"; // TODO put this in GDconventions
    public static final String startTag="started"; // TODO put this in GDconventions
    //public List<String> actualTestMethods = new ArrayList<>();

    List<TestResults> resultsList = new ArrayList<>();


    public TestOrientedAnalyzer() {
        super("Test");
        actualTestMethods = new HashSet<>();
        analyzerTag = "[MainAnalyzer] ";
    }

    @Override
    public void showFinalStatistics() {

    }

    @Override
    public void analyze(List<String> files ) {
        if(GreenSourceAPI.operationalBackend){
            grr.test =  Utils.getTest(applicationID, testingFramework.name() , testOrientation.name());
            grr.test = GreenSourceAPI.sendTestToDB(grr.test.toJSONString());
        }
        testOriented(files);
    }

    private FileWriter createFile(String file){
        try {
            File f = new File(file);
            if (!f.exists()){
                f.createNewFile();
            }
            return  new FileWriter(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }





    private  void testOriented( List<String> csvList) throws NullPointerException{
        view.show("Nr of tests: " +csvList.size()+ " tests");
        CsvParserSettings settings = new CsvParserSettings();
        settings.getFormat().setLineSeparator("\n");
        FileWriter fw = createFile(resultDirPath+"/" + testResultsFile);
        FileWriter fwApp = createFile(resultDirPath+"/" + appResultsFile);
        TestResults actualResult = new TestResults();
        for (int csvIndex = 0; csvIndex <csvList.size() ; csvIndex++) {
            actualResult = new TestResults();
            if (!csvList.get(csvIndex).matches(".*.csv.*") || csvList.get(csvIndex).matches(".*Testresults.csv")){
                continue;
            }
            System.out.println("--- " + csvList.get(csvIndex) + " ---");
            CsvParser parser = new CsvParser(settings);
            List<String[]> resolvedData = null;
            try {
                File f = new File(csvList.get(csvIndex));
                resolvedData = parser.parseAll(new FileReader(f.getAbsolutePath()));
            } catch (FileNotFoundException e) {
                System.out.println("[ANALYZER]: File Not Found: There is no " + csvList.get(csvIndex) + " csv file in directory! to generate results");
                continue;
            }
            Set<TrepnCSVMetric> columns = null;
            try {
                columns = Utils.fetchColumns(resolvedData);
                if(columns.isEmpty()){
                    System.out.println("[ANALYZER] Empty csv file. Ignoring results file " + csvList.get(csvIndex) );
                    continue;
                }
            } catch (Exception e) {
                System.out.println("[ANALYZER] Error fetching columns. Result csv might have an error");
            }
            String number = csvList.get(csvIndex).replaceAll(".+GreendroidResultTrace(.+)\\..+", "$1");
            for (int i = 3; i < resolvedData.size(); i++) {
                String[] row = resolvedData.get(i);
                if (row.length==0 || row[0]==null)
                    break;
                else {
                    getSamplesFromRow(actualResult,columns, row);
                }
            }
            Path p = getRespectiveTracedMethodsFile(csvList.get(csvIndex));
            double totalconsumption = 0.0;
            try{
                 totalconsumption = actualResult.getTotalConsumptionInterpolated( getMetric(Utils.batteryPower, columns ).getSamplesMap(), getMetric(Utils.stateInt, columns ).getSamplesMap() );
            }catch (IllegalArgumentException e){
                System.out.println("[Analyzer] severe Error. The file " + csvList.get(csvIndex) + " is possibly corrupted. Ignoring test"   );
                continue;
            }
            actualResult.testName = getTestName(number);
            actualResult.testId= number;
            //Set<IGreenSourceFormat> ll = actualResult.getAllGSMetrics(columns, be);
            //Map<String,String> resumedTestMetrics = actualResult.showGlobalData(allmethods.getCoverageReferenceNumber());
            if (actualResult.time<=0) {
                view.show("[MainAnalyzer] Warning: Ignoring test " + number + "; Missing start or stop tags in resultant .csv file");
                testNameNumber.put(getTestName(number), number);
                view.showHeadedString("Method Coverage of Test","percentage: " + (setInvokedMethodsAndmethodCoverageTestOriented(actualResult,p) * 100) + " %" );
                //globalReturnList.put(number);
                continue;
            }

            try {
                alltests = loadTests(csvList.get(csvIndex));
            } catch (Exception e) {
                System.out.println("[ANALYZER] Error tracing tests... Assuming order of tests instead of names");
            }
            double testtotalcoverage = (setInvokedMethodsAndmethodCoverageTestOriented(actualResult,p) * 100);
            view.showTestResume( getTestName(number), String.valueOf( totalconsumption) ,String.valueOf( ((TestResults) actualResult).time), String.valueOf(testtotalcoverage) );
            testNameNumber.put(getTestName(number), number);
            copyToAllTracedMethods(actualResult.invokedMethods, number);
            sendIndividualTestResults(actualResult,number, csvIndex, columns, testtotalcoverage );
            if (!GreenSourceAPI.operationalBackend){
                // metrics to file
                JSONArray test_metrics = new JSONArray();
                actualResult.getAllGSMetrics( testtotalcoverage, columns,  loadBeginState( number), loadEndState(number) ).stream().map(x -> x.toGSJSONFormatTestMetric(number.toString())).collect(Collectors.toSet()).forEach( x -> test_metrics.add(((JSONObject) x)));
                try {
                    Utils.writeFile( new File(resultDirPath + "/test"+number+"resume.json"), test_metrics.toJSONString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
//------------------------------------------------------------------------
// end of csv file processing [END]
// ----------------------
// final TrepnResults
        /*l.clear();
        for (Map<String,String> resu : globalReturnList.values()) {
            for (String s : actualResult.metricsName) {
                l.add(resu.get(s));
            }
            Utils.appendToFile(fw,l);
        }
*/
        view.showHeadedString("Total Coverage", "percentage: " + (totalCoverage()*100) + " %");
        try { //TODO change this to another format
            Utils.writeFile( new File(resultDirPath + "/total_coverage.log"), String.valueOf(  (totalCoverage()*100) ));
        } catch (IOException e) {
            e.printStackTrace();
        }
       /*
        l.add("\n------------\n");
        Utils.appendToFile(fw,l);
        l.add("Total method coverage"); l.add(String.valueOf(0));l.add(String.valueOf(0)); l.add( String.valueOf(0)); l.add(String.valueOf((totalCoverage()*100)));
        Utils.appendToFile(fw,l);
        Utils.appendToFile(fwApp,l);
        Utils.writeAndClose(fwApp,l);
        l.clear();
        Utils.writeAndClose(fw,l);*/

        JSONArray jas = new JSONArray();
        jas.addAll(grr.methodsInvoked.values());
        Utils.writeJSONMethodAPIS( jas, resultDirPath+"/" + "methodsInvoked.json" );
        //grr.classMetrics.addAll(getClassMetrics());
        //clean class metrics
        grr.classMetrics = grr.getUniqueClassMetrics();
        sendGlobalTestResults();

    }

    public int getTracedMethodCount(){
        int i = 0;
        for (Map<String,Integer> x : allTracedMethods.values() ){
            i+= x.size();
        }
        return i;
    }

    private static TrepnCSVMetric getMetric(String metricregex, Set<TrepnCSVMetric> m){
       return m.stream().filter(x -> x.metricId.matches(metricregex) ).findFirst().get();
    }

    public void sendGlobalTestResults(){
        view.showHeadedString("Total traced methods", String.valueOf( getTracedMethodCount()) );
        try { //TODO change this to another format
            Utils.writeFile( new File(resultDirPath + "/total_traced_methods.log"), String.valueOf( getTracedMethodCount() ));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (GreenSourceAPI.operationalBackend){
            GreenSourceAPI.sendTestsMetricsToDB(grr.testMetrics.toJSONString());
            GreenSourceAPI.sendClassesToDB(GreenSourceAPI.jsonObjToArray(grr.classes).toJSONString());
            //grr.classImports = getAllImports(grr.classes.values());
            //GreenSourceAPI.sendImportsToDB(grr.classImports.toJSONString());
            GreenSourceAPI.sendMethodsToDB(grr.methodsToJSONString());
            GreenSourceAPI.sendMethodsInvokedToDB(GreenSourceAPI.jsonObjToArray(grr.methodsInvoked).toJSONString());
            GreenSourceAPI.sendMethodsMetricsToDB(grr.methodMetrics.toJSONString());
            GreenSourceAPI.sendClassMetricsToDB(grr.classMetrics.toJSONString());
        }
    }


    public void sendIndividualTestResults(TrepnResults actualResult , String testNumber, int j, Set<TrepnCSVMetric> setMetrics, double cov ){
        if (GreenSourceAPI.operationalBackend){
            JSONObject test_begin_state= loadBeginState( testNumber);
            JSONObject test_end_state= loadBeginState( testNumber);
            JSONObject test_global_state = loadDeviceState();
            try {
                int devState = grr.sendDeviceState(test_global_state);
                grr.testResults = (Utils.getTestResult(testNumber, "", grr.getActualTestID(), "trepn", String.valueOf(devState),  test_begin_state));
                if (testingFramework == TestingFramework.JUNIT){
                    Integer x =  Integer.parseInt((String) grr.testResults.get("test_results_unix_timestamp"));
                    grr.testResults.put("test_results_unix_timestamp", x+j);
                    grr.testResults = GreenSourceAPI.sendTestResultToDB(grr.testResults.toJSONString());
                }
                else {
                    grr.testResults = GreenSourceAPI.sendTestResultToDB(grr.testResults.toJSONString());
                }
                String actualTestResId =  grr.testResults.get("test_results_id").toString();
                getMethodsInvokedAndClass( testNumber ,actualTestResId);
                grr.allTestResults.put(grr.testResults, "");
                //resumedTestMetrics.put("Coverage", String.valueOf(((TestResults) actualResult).getCoverage(allmethods.getCoverageReferenceNumber())));
                grr.testMetrics.addAll( actualResult.getAllGSMetrics(cov,setMetrics, test_begin_state, test_end_state ).stream().map(x -> x.toGSJSONFormatTestMetric((grr.testResults.get("test_results_id").toString()))).collect(Collectors.toList()));
            }catch (Exception e ){
                e.printStackTrace();
                System.out.println("\n[ANALYZER] An error occurred during the Communication with" +
                        "the GreenSource. Not submitting  information regarding this test");
                System.out.println("\n[ANALYZER] Hint: you might be submitting an already existent test result");
            }

            // System.out.println("");
        }
    }

    public  double setInvokedMethodsAndmethodCoverageTestOriented(TrepnResults actualResult, Path pathTraced){
        actualTestMethods = new HashSet<>();
        if(pathTraced==null) return 0;
        HashMap<String, Integer> thisTest = new HashMap<>();
        try {
            try (Stream<String> lines = Files.lines (pathTraced, StandardCharsets.UTF_8)) {
                for (String line : (Iterable<String>) lines::iterator) {
                    if( ! allmethods.getMerged().containsKey(line)){
                        System.out.println("Warning: Unmatched method -> " +line );
                        System.out.println("This method was invoked during app execution, but the identifier doesn't match any of" +
                                "identifiers gathered during isntrumentation phase");
                        continue;
                    }

                    if(thisTest.containsKey(line)){
                        int x = thisTest.get(line);
                        thisTest.put(line,++x);
                        actualTestMethods.add(line);
                    }
                    else{
                        thisTest.put(line,1);
                        actualTestMethods.add(line);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        ((TestResults) actualResult).invokedMethods = thisTest;
        double percentageCoverage = ((double)thisTest.size()/(double)(allmethods.getSourceCoverage()));
        return percentageCoverage;

    }

    private void getMethodsInvokedAndClass(String testNumber ,String testResID){
        for (String method_id : this.actualTestMethods) {
            if (allmethods.getMerged().containsKey(method_id)) {
                JSONObject method = ((JSONObject) allmethods.getMerged().get(method_id));
                JSONObject j = methodToGSourceJSONFormat(method);
                JSONObject classOfMethod_formatted = classToGSourceJSONFormat(((JSONObject) allmethods.getApkMethods().get(method.get("method_class"))));
                grr.classes.put(method.get("method_class") , classOfMethod_formatted);
                j.put("method_class", classOfMethod_formatted.get("class_id") );
                grr.methods.put(((String) j.get("method_id")), j);
                grr.methodMetrics.addAll(getMethodMetrics(((String) j.get("method_id")),method, Integer.parseInt(testResID)));
                grr.methodsInvoked.put(j.get("method_id"),Utils.getMethodsInvoked(testResID, ((String) j.get("method_id")), allTracedMethods.get(testNumber).get(method_id).toString() ));
                grr.classMetrics.addAll(getClassMetrics(classOfMethod_formatted, testResID));
                       // add(Utils.getMethodsInvoked(testResID, ((String) j.get("method_id")), allTracedMethods.get(testNumber).get(method_id).toString()), ((String) j.get("method_id")));
            }
            else{
                System.out.println(method_id + " not found");
            }
        }
    }

    private JSONObject buildMethodMetric(String method, String metric, String value_text, int coef, int tesResId){
        JSONObject o = new JSONObject();
        //fields = ('mm_method', 'mm_metric','mm_value_text', 'mm_coeficient','mm_test')
        o.put("mm_method",method);
        o.put("mm_metric", metric );
        o.put("mm_value_text", value_text);
        o.put("mm_coeficient", coef);
        o.put("mm_test_result", tesResId);
        return o;
    }

    private JSONArray getMethodMetrics(String  methodID , JSONObject jsonMethod, int testResId) {
        JSONArray jas = new JSONArray();
        JSONArray apis = ((JSONArray) jsonMethod.get("method_apis"));
        for (Object o :  apis){
            JSONObject api_json = ((JSONObject) o);
            JSONArray args_of_api = ((JSONArray) api_json.get("args"));
            String args = "";
            if (args_of_api!=null){
                StringBuilder sb = new StringBuilder();
                args_of_api.forEach( x -> sb.append("," + x));
                args = sb.toString().replaceFirst(",","");

            }
            String value = api_json.get("name") + "(" + args + ")|"+ api_json.get("return");
            jas.add( buildMethodMetric(methodID, "api", value, 1 , testResId));
        }
        if ( jsonMethod.containsKey("method_nr_instructions") ) {
            int nr_instructions = Integer.parseInt((jsonMethod.get("method_nr_instructions").toString()));
            if (nr_instructions>0){
                jas.add(buildMethodMetric(methodID,"nr_instructions", String.valueOf(nr_instructions), 1 ,testResId));
            }
        }
        if ( jsonMethod.containsKey("method_length") ) {
            int len = Integer.parseInt((jsonMethod.get("method_length").toString()));
            if (len>0){
                jas.add(buildMethodMetric(methodID,"length", String.valueOf(len), 1 ,testResId));
            }
        }
        if ( jsonMethod.containsKey("method_locals") ) {
            int nr_locals = Integer.parseInt((jsonMethod.get("method_locals").toString()));
            if (nr_locals>0){
                jas.add(buildMethodMetric(methodID,"locals", String.valueOf(nr_locals), 1 ,testResId));
            }
        }
        return jas;
    }

    private JSONObject classToGSourceJSONFormat(JSONObject classObj){
        if (classObj==null){
            return classObj;
        }
        JSONObject jo = new JSONObject();
        jo.putAll(classObj);
        jo.put("class_id",applicationID +"--" + ((String)jo.get("class_name")).replaceAll("\\.","--"));
        jo.put("class_app", applicationID);
        return jo;
    }

    private JSONObject methodToGSourceJSONFormat(JSONObject methodObj){
        JSONObject jo = new JSONObject();
        String cla = applicationID +"--" + ((String)methodObj.get("method_class")).replaceAll("\\.","--");
        jo.put("method_class", cla );
        jo.put("method_id", ((String) jo.get("method_class")).hashCode() + "--" +((String) methodObj.get("method_name")) + "--" + ((String) methodObj.get("method_hash")) );
        jo.put("method_modifiers", methodObj.get("method_modifiers"));
        jo.put("method_name", methodObj.get("method_name"));
        StringBuilder sb = new StringBuilder();
        if (! ((JSONArray) methodObj.get("method_args")).isEmpty() ){
            for (Object o : ((JSONArray) methodObj.get("method_args"))){
                sb.append("--"+o);
            }
            jo.put("method_args", sb.toString().replaceFirst("--",""));
        }
        else {
            jo.put("method_args", null);
        }
        jo.put("method_return", methodObj.get("method_return"));
        return jo;
    }

    public static Path getRespectiveTracedMethodsFile(String csvFile){
        String number =csvFile.replaceAll(".+GreendroidResultTrace(.+)\\..+","$1");
        File f = new File(csvFile);
        Path path = Paths.get(f.getAbsoluteFile().getParent());
        Path p = null;
        try{
            DirectoryStream<Path> stream;
            stream = Files.newDirectoryStream(path);
            for (Path entry : stream)
            {
                if(entry.getFileName().toString().matches("TracedMethods"+number+".txt")){
                    p = entry;//break;
                    break;
                }
            }
            stream.close();
            if (p==null)
                throw new IOException("TracedMethods"+number+".txt not found");
        }
        catch (IOException e) {
            System.out.println( " : File containing traced Methods of file (TracedMethods"+number+".txt) not found! Assumed 0 traced methods");
        }
        return p;
    }

    public  void getSamplesFromRow(TrepnResults actualResult , Set<TrepnCSVMetric>columns, String[] row) {
        for (TrepnCSVMetric tcv : columns){
            if (tcv.getValue_csv_column() < row.length && row[tcv.getValue_csv_column()] !=null ){
                if (tcv.metricId.matches(Utils.stateDescription)){
                    if (!(actualResult).hasStartTime()&& row[tcv.getValue_csv_column()].equals(startTag)){
                        ((TestResults) actualResult).startTime = Integer.parseInt(row[tcv.getTime_csv_column()]);
                    }
                    else if (!(actualResult).hasEndTime()&& row[tcv.getValue_csv_column()].equals(stopTag)){
                        ((TestResults) actualResult).stopTime = Integer.parseInt(row[tcv.getTime_csv_column()]);
                    }
                }
                else {
                    tcv.addSample( Integer.parseInt( row[tcv.getTime_csv_column()]), Long.valueOf( row[tcv.getValue_csv_column()] ));
                }
            }
        }
    }

    @Override
    public void buildAnalysisJSONFile() {
        JSONObject mainJO = new JSONObject();
        mainJO.put("test_tool", testingFramework);
        mainJO.put("test_orientation", testOrientation);
        JSONObject joca = new JSONObject();
        resultsList.forEach( x ->  joca.put( x.testId, x.toAnalysisJSONFile()  ));
        mainJO.put("results", joca );
    }

}
