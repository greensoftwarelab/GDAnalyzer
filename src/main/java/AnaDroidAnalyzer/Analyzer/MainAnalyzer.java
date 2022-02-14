package AnaDroidAnalyzer.Analyzer;


import AnaDroidAnalyzer.Analyzer.Views.TerminalView;
import AnaDroidAnalyzer.Analyzer.Views.View;
import AnaDroidAnalyzer.GreenSourceBridge.GDConventions;
import AnaDroidAnalyzer.GreenSourceBridge.GreenSourceAPI;
import AnaDroidAnalyzer.Results.JSONsMerger;
import AnaDroidAnalyzer.Utils.Utils;
import AndroidProjectRepresentation.APICallUtil;
import AndroidProjectRepresentation.ClassInfo;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainAnalyzer implements PowerLogAnalyzer {

    // static fields
    public static final String appIssuesFile = GDConventions.IssuesOutputName;
    public static final String testResultsFile = GDConventions.TestOutputName;
    public static final String appResultsFile = GDConventions.AppOutputName;
    private static boolean mergeOldRuns= true;

    public String analyzerTag= "[MainAnalyzer] ";
    public String folderPrefix = "Test";
    public String pathPermissions = "";
    public String pathApp = "";
    public String pathDevice = "";
    public String pathDeviceState= "";
    public String applicationID = "unknown";
    public String projectAppJSONFile ="";
    public String resultDirPath = "results/" ;
    public String appPackage = "?" ;
    private String pathLogin =null;
    public View view = new TerminalView();


    public TestingFramework testingFramework = TestingFramework.MONKEY;
    public TestOrientation testOrientation = TestOrientation.TESTORIENTED;
    public JSONsMerger allmethods= new JSONsMerger();
    public List<String> alltests= new ArrayList<>();
    public GreenSourceAPI grr = new GreenSourceAPI();
    public APICallUtil acu = null;
    public  HashMap<String ,String>  testNameNumber = new HashMap<>();
    public  HashMap<String, Map <String,Integer>> allTracedMethods = new HashMap<>(); // testnumber ->( Method name -> times invoked)
    //
    public static int stoppedState = 0;
    public  HashSet<String> actualTestMethods = new HashSet<>();
    public  String stopTag="stopped"; // TODO put this in GDconventions
    public  String startTag="started"; // TODO put this in GDconventions
    public  JSONArray energyGreadyAPIS = new JSONArray();
    public  Hashtable<String, String> deviceStatesFiles = new Hashtable(); //?
    public  Set<ClassInfo> classesSet = new HashSet<>();
    public  Map<String, Integer> methodsSet = new HashMap<>();


    enum TestingFramework{
        MONKEY,
        MONKEYRUNNER,
        JUNIT,
        RERAN,
        CRAWLER,
        NONE
    }

    enum TestOrientation{
        TESTORIENTED,
        METHODORIENTED,
        APPLICATIONORIENTED,
        ACTIVITYORIENTED
    }

    public MainAnalyzer() {
    }

    public MainAnalyzer(String folderPrefix) {
        this.folderPrefix=folderPrefix;
    }

    public boolean isTestOriented() {
        return this instanceof TestOrientedAnalyzer;
    }



    protected List<String> loadTests(String csvFile) throws Exception {
        alltests = new ArrayList<>();
        File f = new File(csvFile);
        Path path = Paths.get(f.getAbsoluteFile().getParent());
        Path p = null;
        try{
            DirectoryStream<Path> stream;
            stream = Files.newDirectoryStream(path);
            for (Path entry : stream)
            {
                if(entry.getFileName().toString().matches("TracedTests\\.txt")){
                    p = entry;//break;
                    break;
                }
            }
            stream.close();
            if (p==null)
                throw new IOException("TracedTests not found");
        }
        catch (IOException e) {
            System.out.println("TracedTests not found");
            return alltests;
        }
        try (Stream<String> lines = Files.lines (p, StandardCharsets.UTF_8)) {
                for (String line : (Iterable<String>) lines::iterator) {
                    alltests.add(line);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        return alltests;
    }

    /*
    public String processMethodInvoked (String methodName,String className){
       // System.out.println(acu.proj.getCurrentApp().allJavaClasses);
        MethodInfo mi = acu.getMethodOfClass(methodName,className);
        String metId= mi.getMethodID();
        //System.out.println(acu.proj.apps);
        if (methodsSet.containsKey(metId)){
           methodsSet.put(metId ,methodsSet.get(metId)+1);
        }
        else {
            methodsSet.put(metId,1);
            if(mi.ci!=null){
                grr.methods.add(mi.toJSONObject(mi.ci.getClassID()),  mi.getMethodID());
                grr.methodMetrics.addAll(Utils.getMethodsMetrics(mi));
                if( !classesSet.contains(mi.ci)){
                    grr.classes.add(mi.ci.toJSONObject(applicationID),  mi.ci.getClassID());
                    classesSet.add(mi.ci);
                }
            }
        }
        return metId;
    }*/

    public JSONObject getClassMetric(String classID, String metric  ,String value_text, int coef, int testResId){
        JSONObject jo = new JSONObject();
        jo.put("cm_class",classID);
        jo.put("cm_metric",metric);
        jo.put("cm_coeficient", coef);
        jo.put("cm_value_text",value_text );
        jo.put("cm_test_result",testResId );
        return jo;
    }


    public JSONArray getClassMetrics(JSONObject classObj, String testResId ) {
        JSONArray ja  = new JSONArray();
        String classId = ((String ) classObj.get("class_id"));
        ja.add(getClassMetric(classId, "nr_methods", String.valueOf (((JSONObject) classObj.get("class_methods")).size()),1, Integer.parseInt(testResId) ));
        JSONArray class_fields = ((JSONArray) classObj.get("class_fields"));
        if ( ! class_fields.isEmpty() ){
            StringBuilder sb = new StringBuilder();
            for (Object o :  class_fields){
                sb.append(","+o);
            }
            ja.add(getClassMetric(classId, "field", sb.toString().replaceFirst(",",""),1 , Integer.parseInt(testResId)));
        }
        JSONArray ifaces = ((JSONArray) classObj.get("class_implemented_ifaces"));
        if (! ifaces.isEmpty()){
            String value = ((String) ((JSONArray) classObj.get("class_implemented_ifaces")).stream().map(Object::toString).collect(Collectors.joining("--")));
            ja.add(getClassMetric(classId, "implements", value,1,Integer.parseInt(testResId) ));
        }
        String s = classObj.get("class_superclass").toString();
        if ( !s.isEmpty() ){
            ja.add(getClassMetric(classId, "superclass", s,1 , Integer.parseInt(testResId)));
        }

        return ja;
        //return new JSONArray();
    }


    private void loadMethods() {
        this.allmethods = new JSONsMerger(resultDirPath+"/all/"+ appPackage +".json", resultDirPath+"/all/allMethods.json");
        this.allmethods.merge();
    }


    protected void copyToAllTracedMethods (Map<String, Integer> h, String testnumber) {
        if (allTracedMethods.containsKey(testnumber)){
            for (String s : h.keySet()) {
                if (allTracedMethods.get(testnumber).containsKey(s)){
                    int x = allTracedMethods.get(testnumber).get(s);
                    int y = h.get(s);
                    allTracedMethods.get(testnumber).put(s, y+x);
                }
                else {
                    allTracedMethods.get(testnumber).put(s, h.get(s));
                }
            }
        }
        else{
            HashMap<String,Integer> mm = new HashMap<>();
            for (String s : h.keySet()) {
                if (mm.containsKey(s)){
                    int x = mm.get(s);
                    int y = h.get(s);
                    mm.put(s, y+x);
                }
                else {
                    mm.put(s, h.get(s));
                }
            }
            allTracedMethods.put(testnumber, mm );
        }
    }


    public JSONObject loadEndState(String number) {
        if (testingFramework == TestingFramework.JUNIT){
            String key = deviceStatesFiles.keySet().stream().filter(x -> x.matches("end_state[0-9]*.json")).findFirst().get();
            return grr.loadDeviceState(deviceStatesFiles.get(key));
        }else{
            return grr.loadDeviceState(deviceStatesFiles.get("end_state"+ number+".json"));
        }
        /*
        else if (testingFramework == TestingFramework.MONKEYRUNNER){
            return grr.loadDeviceState(deviceStatesFiles.get("end_state"+ number+".json"));
        }
        else if (testingFramework == TestingFramework.MONKEY){
            return grr.loadDeviceState(deviceStatesFiles.get("end_state"+ number+".json"));
        }

        else if (testingFramework == TestingFramework.NONE){
            return grr.loadDeviceState(deviceStatesFiles.get("end_state"+ number+".json"));
        }
        */
       // return new JSONObject();
    }

    public JSONObject loadBeginState(String number) {
        if (testingFramework == TestingFramework.JUNIT){
            String key = deviceStatesFiles.keySet().stream().filter(x -> x.matches("begin_state[0-9]*.json")).findFirst().get();
            return grr.loadDeviceState(deviceStatesFiles.get(key));
        }/*
        else if (testingFramework == TestingFramework.MONKEYRUNNER){
            return grr.loadDeviceState(deviceStatesFiles.get("begin_state"+ number+".json"));
        }
        else if (testingFramework == TestingFramework.MONKEY){
            return grr.loadDeviceState(deviceStatesFiles.get("begin_state"+ number+".json"));
        }
        else if (testingFramework == TestingFramework.NONE){
            return grr.loadDeviceState(deviceStatesFiles.get("begin_state"+ number+".json"));
        }*/
        else {
            return grr.loadDeviceState(deviceStatesFiles.get("end_state"+ number+".json"));
        }
        //return new JSONObject();
    }

    public JSONObject loadDeviceState() {
       return Utils.loadJSONObj(pathDeviceState);
    }



    public static JSONObject getAllImports(Collection<Object> jc){
        JSONObject jco = new JSONObject();
        try {
            for (Object jj : jc){
                JSONObject jo = ((JSONObject) jj);
                JSONArray j = ((JSONArray) jo.get("class_imports"));
                for (Object obj : j){
                    JSONObject jop = ((JSONObject) obj);
                    jco.put(jop.get("import_name").toString() +jop.get("import_class").toString() , jop);
                }
            }
        }
        catch (Exception e){

        }
        return jco;
    }


    public double totalCoverage(){
        HashSet<String> hashSet = new HashSet<>();
        allTracedMethods.values().stream().forEach(x ->  hashSet.addAll(x.keySet())  );
        double percentageCoverage = ((double)hashSet.size()/(double)(allmethods.getSourceCoverage()));
        return percentageCoverage;
    }

    public String getTestName(String number){
        if(!alltests.isEmpty() && Integer.parseInt(number)<= alltests.size()){
            return alltests.get(Integer.parseInt(number));
        }
        else
            return number;
    }

    public static String getAppPackage(String resultDirPath){
        ArrayList<String> l = new ArrayList<>();
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(
                Paths.get(resultDirPath+"/all"), "*.json")) {
            dirStream.forEach(x -> l.add(x.getFileName().toString()) );
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (l.size() <= 1){
            System.out.println("Warning: incomplete \"all \" folder");
        }
        String s = l.stream().filter(x -> !x.contains("allMethods")).collect(Collectors.toList()).get(0).replace(".json","");
        return s;
    }

    public  List <String> fetchAllCSVs(){
        List <String> list = new ArrayList<>( );
        Path path = Paths.get(resultDirPath);
        try{
            DirectoryStream<Path> stream;
            stream = Files.newDirectoryStream(path);
            // foreach file in resulDir folder
            for (Path entry : stream) {
                if( ! entry.getFileName().toString().toLowerCase().startsWith(testingFramework.name().toLowerCase())  && testingFramework!=TestingFramework.NONE ){
                    continue;
                }
                if( (Files.isDirectory(entry) || Files.isSymbolicLink(entry) ) &&  entry.getFileName().toString().toLowerCase().startsWith(testingFramework.name().toLowerCase() + "test")  && testingFramework != TestingFramework.NONE ) { // if is a folder
                    //get files of that folder
                    DirectoryStream<Path> streamChild = null;
                    try {
                        if (Files.isSymbolicLink(entry)){
                            //System.out.println(Files.readSymbolicLink(entry).toString());
                            streamChild = Files.newDirectoryStream(Files.readSymbolicLink(entry).toAbsolutePath());
                        }
                        else{
                            streamChild = Files.newDirectoryStream(Paths.get(entry.toString()));
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        return list;
                    }
                    for (Path entry1 : streamChild) { //foreach file of that folder
                        //System.out.println(entry1.getFileName().toString());
                        if (entry.equals(path))
                            continue;
                        if (entry1.getFileName().toString().matches("GreendroidResultTrace[0-9]+\\.csv")) {
                            list.add(entry1.toString());
                        }
                        if (entry1.getFileName().toString().matches("begin_state[0-9]*\\.json")) {
                            deviceStatesFiles.put(entry1.getFileName().toString(),entry1.toString());
                        }
                        if (entry1.getFileName().toString().matches("end_state[0-9]*\\.json")) {
                            deviceStatesFiles.put(entry1.getFileName().toString(),entry1.toString());
                        }

                        if (entry1.getFileName().toString().matches("appPermissions.json")) {
                            pathPermissions = entry1.toString();
                            //System.out.println(pathPermissions);
                        }
                        if (entry1.getFileName().toString().contains(GDConventions.fieldDelimiter)) {
                            if (entry1.getFileName().toString().contains(".json")){
                                pathApp=entry1.toString();
                                projectAppJSONFile =pathApp;
                            }
                        }
                        if (entry1.getFileName().toString().matches("device.json")) {
                            pathDevice= entry1.toString();
                        }
                        if (entry1.getFileName().toString().matches("deviceState.json")) {
                            pathDeviceState= entry1.toString();
                        }
                        if (entry1.getFileName().toString().matches("GSlogin.json")) {
                            pathLogin= entry1.toString();
                        }
                        if (entry1.getFileName().toString().matches(".json")) {
                            pathApp=entry1.toString();
                        }

                    }
                }
                else {
                    if (entry.equals(path))
                        continue;
                    if (entry.getFileName().toString().matches("GreendroidResultTrace[0-9]+\\.csv")) {
                        list.add(entry.toString());
                    }
                    if (entry.getFileName().toString().matches("begin_state[0-9]*\\.json")) {
                        deviceStatesFiles.put(entry.getFileName().toString(),entry.toString());
                    }
                    if (entry.getFileName().toString().matches("end_state[0-9]*\\.json")) {
                        deviceStatesFiles.put(entry.getFileName().toString(),entry.toString());
                    }
                    if (entry.getFileName().toString().matches("appPermissions.json")) {
                        pathPermissions = entry.getFileName().toString();
                       // System.out.println(pathPermissions);
                    }
                    if (entry.getFileName().toString().contains(GDConventions.fieldDelimiter)) {
                        if (entry.getFileName().toString().contains(".json")){
                            pathApp=entry.toString();
                            projectAppJSONFile =pathApp;
                        }
                    }
                    if (entry.getFileName().toString().matches("device.json")) {
                        pathDevice= entry.getFileName().toString();
                       // System.out.println(pathDevice);
                    }
                    if (entry.getFileName().toString().matches("deviceState.json")) {
                        pathDeviceState= entry.getFileName().toString();
                        // System.out.println(pathDevice);
                    }
                    if (entry.getFileName().toString().matches("GSlogin.json")) {
                        pathLogin= entry.getFileName().toString();
                        // System.out.println(pathDevice);
                    }
                }
            }
            stream.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        if (mergeOldRuns){
            resultDirPath+="oldRuns/";
            mergeOldRuns = false;
            List<String> x = fetchAllCSVs();
            list.addAll(x);
        mergeOldRuns = true;
        }
        try{
            JSONObject jj =  new APICallUtil().fromJSONFile(projectAppJSONFile);
            acu = ((APICallUtil) new APICallUtil().fromJSONObject(jj));
            appPackage =  acu.proj.getCurrentApp().appPackage;
            if (appPackage==""){
                appPackage=getAppPackage(resultDirPath);

            }
        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(-1);
        }
        return list;
    }

    public static String getSimpleProjectJSON (JSONObject jo ){
        JSONObject pro = new JSONObject();
        pro.put("project_id", jo.get("project_id") );
        pro.put("project_build_tool", jo.get("project_build_tool") );
        pro.put("project_description", jo.get("project_description") );
        return pro.toJSONString();
    }

    public void sendApplicationInformation(){
        if (GreenSourceAPI.operationalBackend){
            this.grr.loginGreenSource(pathLogin);
            JSONObject jo = acu.proj.toJSONObject("");
            String projectToSend = getSimpleProjectJSON(jo);
            grr.project =  GreenSourceAPI.sendProjectToDB(projectToSend);
            this.applicationID = (String) (acu.proj.apps.get(0).toJSONObject(acu.proj.projectID)).get("app_id");
            grr.app=GreenSourceAPI.sendApplicationToDB(acu.proj.getCurrentApp().toJSONObject(acu.proj.projectID).toJSONString());
            JSONArray permissions = GreenSourceAPI.loadAppPermissions(pathPermissions);
            modifyJSONPermissions(permissions, this.applicationID);
            GreenSourceAPI.sendAppPermissionsToDB( permissions.toJSONString() );
            grr.device=Utils.loadJSONObj(this.pathDevice);
            GreenSourceAPI.sendDeviceToDB( grr.device);
            GreenSourceAPI.sendDeviceState( Utils.loadJSONObj(pathDeviceState));
        }
    }

    public static void modifyJSONPermissions(JSONArray permissions, String apid){
        permissions.stream().forEach(x -> ((JSONObject) x).replace("application", apid));

    }

    public static MainAnalyzer initDefault(String testOrientat, String resultDirPath){
        MainAnalyzer m = null;
        if (testOrientat.equals("-TestOriented")){
            m = new TestOrientedAnalyzer();
            m.testOrientation=TestOrientation.TESTORIENTED;
        }
        else if (testOrientat.equals("-MethodOriented")){
            m = new MethodOrientedAnalyzer();
            m.testOrientation=TestOrientation.METHODORIENTED;

        }
        else if (testOrientat.equals("-AppOriented")){
            m =  new MainAnalyzer();
            m.testOrientation=TestOrientation.APPLICATIONORIENTED;
        }
        else if (testOrientat.equals("-ActivityOriented")){
            m =  new ActivityOrientedAnalyzer();
            m.testOrientation=TestOrientation.ACTIVITYORIENTED;
        }
        m.mergeOldRuns = false; // TODO
        m.grr = new GreenSourceAPI();
        m.grr.setGreenRepoUrl("http://localhost:8000");
        m.grr.operationalBackend=true;
        m.resultDirPath = resultDirPath;
        return m;
    }

    @Override
    public void showFinalStatistics() {
        return;
    }

    @Override
    public void analyze(List<String> files) {
        if(GreenSourceAPI.operationalBackend){
            grr.test =  Utils.getTest(applicationID, testingFramework.name() , testOrientation.name());
            grr.test = GreenSourceAPI.sendTestToDB(grr.test.toJSONString());
            System.out.println("sended");
        }
       return;
    }


    public void setTestingFramework(String framework){
        if (framework.toLowerCase().equals("-monkey")){
            testingFramework = TestingFramework.MONKEY;
            stoppedState = 2;
        }
        else if (framework.toLowerCase().equals("-monkeyrunner")){
            testingFramework = TestingFramework.MONKEYRUNNER;
        }
        else if (framework.toLowerCase().equals("-junit")){
            testingFramework = TestingFramework.JUNIT;
        }
        else if (framework.toLowerCase().equals("-reran")){
            testingFramework = TestingFramework.RERAN;
        }
        else if (framework.toLowerCase().contains("crawler")){
            testingFramework = TestingFramework.CRAWLER;
        }
        else if (framework.toLowerCase().equals("-none")){
            testingFramework = TestingFramework.NONE;
        }

    }

    public void setGreenSourceServer(String greenSourceServer) {
        if(greenSourceServer.equals("localhost") ){
            this.grr.setGreenRepoUrl("http://localhost:8000/");
        }
        else if(greenSourceServer.equals("NONE")){
            this.grr.setGreenRepoUrl("NONE");
            GreenSourceAPI.operationalBackend=false;
        }
        else if( ! greenSourceServer.equals("") ){
            this.grr.setGreenRepoUrl(greenSourceServer);
        }
       // this.grr.setGreenRepoUrl(greenSourceServer);
    }


    public static void main(String[] args) {
        if (args.length<4){
            System.out.println("Bad argument length for Greendroid MainAnalyzer! Usage ->  [-(Test|Method)Oriented] resultsdir [-Monkey|JUnit]");
            System.exit(0);
        }
        MainAnalyzer mainAnalyzer = MainAnalyzer.initDefault(args[0],args[1]);
        mainAnalyzer.setTestingFramework(args[2]);
        mainAnalyzer.setGreenSourceServer(args[3]);
        System.out.println("Sending results to -> " + args[3]);
        try {
            List<String> allcsvs = mainAnalyzer.fetchAllCSVs();
            mainAnalyzer.loadMethods();
            mainAnalyzer.sendApplicationInformation();
            mainAnalyzer.analyze(allcsvs);
            mainAnalyzer.buildAnalysisJSONFile();
            mainAnalyzer.logCalledMethods( mainAnalyzer.resultDirPath + "/" + "allTracedMethods.json"); // TODO pass to config file
        } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    public void buildAnalysisJSONFile() {


    }


    public void logCalledMethods(String filename  ){
       JSONObject jo = new JSONObject();
        for (String testId  : allTracedMethods.keySet()){
            JSONObject singleTestcalledMethods= new JSONObject();
            if (allTracedMethods.get(testId).isEmpty()){
                continue;
            }
            for(String methodId : allTracedMethods.get(testId).keySet()){
                JSONObject correspondantMethod = ((JSONObject) allmethods.getMerged().get(methodId));
                if (correspondantMethod!=null){
                    singleTestcalledMethods.put( methodId, correspondantMethod );
                }
            }

            jo.put(testId, singleTestcalledMethods);
        }
        Utils.writeJSONObject(jo, filename);
    }


}



