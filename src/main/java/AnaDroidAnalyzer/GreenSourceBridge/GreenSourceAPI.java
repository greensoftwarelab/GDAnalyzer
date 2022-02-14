package AnaDroidAnalyzer.GreenSourceBridge;

import AnaDroidAnalyzer.Analyzer.MainAnalyzer;
import AnaDroidAnalyzer.Utils.Utils;
import AndroidProjectRepresentation.MethodInfo;
import AndroidProjectRepresentation.Variable;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class GreenSourceAPI {

    public static boolean operationalBackend = true;
    //private static final String greenRepoURL = "http://localhost:8000/";
    private static String greenRepoURL = "http://greensource.di.uminho.pt/";
    private static String testUlr = "tests/";
    private static String appsUlr = "apps/";
    private static String projectsUlr = "projects/";
    private static String testsResultsUlr = "tests/results/";
    private static String appPermissionsUrl = "apps/permissions/";
    private static String devicesUrl = "devices/";
    private static String deviceStatesUrl = "devicestate/";
    private static String methodsUrl = "methods/";
    private static String classesUrl = "classes/";
    private static String methodsMetricsUrl = "methods/metrics/";
    private static String appsMetricsUrl = "apps/metrics/";
    private static String classesMetricsUrl = "classes/metrics/";
    private static String methodsInvokedUrl = "methods/invoked/";
    private static String testsMetricsUrl = "tests/metrics/";
    private static String importsURL = "imports/";
    private static String loginURL = "login/";
    private  static String JWTtoken = "";

    public JSONObject project;
    public JSONObject app;
    public JSONObject device;
    public JSONObject test;
    public JSONObject testResults;
    public JSONObject allTestResults;
    public Map<String, JSONObject> methods;
    public JSONObject classes;
    public JSONArray methodMetrics;
    public JSONArray classMetrics;
    public JSONArray appMetrics;
    public JSONArray testMetrics;
    public JSONObject deviceStateEnd;
    public JSONObject deviceStateBegin;
    public JSONObject methodsInvoked;
    public JSONObject classImports;

    public GreenSourceAPI() {
        this.project = new JSONObject();
        this.app = new JSONObject();
        this.test = new JSONObject();
        this.testResults = new JSONObject();
        this.methods = new HashMap<>();
        this.classes = new JSONObject();
        this.classMetrics = new JSONArray();
        this.appMetrics = new JSONArray();
        this.methodMetrics = new JSONArray();
        this.testMetrics = new JSONArray();
        this.methodsInvoked = new JSONObject();
        this.deviceStateBegin = new JSONObject();
        this.deviceStateEnd = new JSONObject();
        this.device=new JSONObject();
        this.allTestResults = new JSONObject();
        this.classImports = new JSONObject();
    }

    public String getActualTestID(){
        if (this.test!=null){
            if (this.test.containsKey("id"))
                return this.test.get("id").toString();
        }
        return "-1";
    }



    public String getActualTestResultsID(){
        if (this.testResults.containsKey("test_results_id"))
            return this.testResults.get("test_results_id").toString();
        return "-1";
    }


    public String getActualDeviceID(){
        if (this.device.containsKey("device_serial_number"))
            return this.device.get("device_serial_number").toString();
        return "ERROR";
    }


    public String methodsToJSONString(){
        JSONArray ja = new JSONArray();
        ja.addAll(methods.values());
        return ja.toJSONString();
    }



    public String getGreenRepoURL(){
     return greenRepoURL;
    }

    public void setGreenRepoUrl(String url){
        greenRepoURL= url;
    }



    public static JSONArray loadAppPermissions(String appPermissionsJSONFile){
        JSONParser parser = new JSONParser();
        JSONArray ja = new JSONArray();
        try {
            Object obj = parser.parse(new FileReader(appPermissionsJSONFile));
            JSONArray jsonObject = (JSONArray) obj;
            ja = (JSONArray) jsonObject;

        } catch (FileNotFoundException e) {
            System.out.println("[ANALYZER] No appPermissions.json file. File not found");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return ja;
    }




    public static JSONObject loadDevice(String deviceJSONFile){
        JSONParser parser = new JSONParser();
        JSONObject ja = new JSONObject();
        try {
            Object obj = parser.parse(new FileReader(deviceJSONFile));
            JSONObject jsonObject = (JSONObject) obj;
            if (!jsonObject.containsKey("device_serial_number")){
                System.out.println("FATAL ERROR! Error in application json");
            }
            else
                return jsonObject;

        } catch (FileNotFoundException e) {
            System.out.println("[ANALYZER] No device.json file. File not found");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return ja;
    }


    public JSONObject loadDeviceState(String deviceStateFile) {
        return new DeviceTestState().fromJSONFile(deviceStateFile);
    }

    public static JSONObject loadApplication(String appJSONFile){
        JSONParser parser = new JSONParser();
        JSONObject ja = new JSONObject();
        try {
            Object obj = parser.parse(new FileReader(appJSONFile));
            JSONObject jsonObject = (JSONObject) obj;
            if (!jsonObject.containsKey("app_id")){
                System.out.println("FATAL ERROR! Error in application json");
            }
            else
                return jsonObject;

        } catch (FileNotFoundException e) {
            System.out.println("[ANALYZER] No application.json file. File not found");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return ja;
    }

    public static Object sendImportsToDB(String json) {

        if (operationalBackend){
            String response = sendJSONtoDB(greenRepoURL + importsURL, json).second;
            JSONParser parser = new JSONParser();
            //JSONArray jsonObject = new JSONArray();
            Object jsonObject;
            try {
                Object obj = parser.parse(response);
               jsonObject = (Object) obj;

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
            return new JSONObject();

    }



    public static JSONObject sendTestToDB(String json) {

       if (operationalBackend){
           String response = sendJSONtoDB(greenRepoURL + testUlr, json).second;
           JSONParser parser = new JSONParser();
           JSONObject jsonObject = new JSONObject();
           try {
               Object obj = parser.parse(response);
               jsonObject = (JSONObject) obj;

           } catch (ParseException e) {
               e.printStackTrace();
           }

           if (!jsonObject.containsKey("id")) {
               System.out.println("FATAL ERROR ! There is an error in test JSON. Not submitting results do DBase");
               return null;

           } else return jsonObject;
       }
       else
           return new JSONObject();

    }






    public static JSONObject sendTestResultToDB(String json) {
        if (operationalBackend){
            String response = (sendJSONtoDB(greenRepoURL + testsResultsUlr, json)).second;
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = new JSONObject();
            try {
                Object obj = parser.parse(response);
                jsonObject = (JSONObject) obj;

            } catch (ParseException | ClassCastException e ) {
                e.printStackTrace();
            }

            if (!jsonObject.containsKey("test_results_id")) {
                System.out.println("FATAL ERROR ! There is an error in test results JSON. Not submitting results do DBase");
                return null;

            } else return jsonObject;
        }
        else
            return new JSONObject();

    }


    public static JSONArray sendTestResultsToDB(String json) {

        if (operationalBackend){
            Pair<Integer,String> res = sendJSONtoDB(greenRepoURL + testsResultsUlr, json);
            JSONParser parser = new JSONParser();
            JSONArray jsonObject = new JSONArray();
            if (res.first!=200) {
                System.out.println("FATAL ERROR ! HTTP CODE != 200 . something went wrong check internet connection or allTestResults JSON FILE");
                return null;
            }
            try {
                Object obj = parser.parse(res.second);
                jsonObject = (JSONArray) obj;

            } catch (ParseException | ClassCastException e ) {
                e.printStackTrace();
            }

            return jsonObject;
        }
        else
            return new JSONArray();

    }

    public static JSONArray sendMethodsToDB(String json) {
        if (operationalBackend){
            Pair<Integer,String> res = sendJSONtoDB(greenRepoURL + methodsUrl, json);
            JSONParser parser = new JSONParser();
            JSONArray jsonObject = new JSONArray();
            if (res.first!=200) {
                System.out.println("FATAL ERROR ! HTTP CODE != 200 . something went wrong check internet connection or methods JSON FILE");
                return null;
            }
            try {
                Object obj = parser.parse(res.second);
                jsonObject = (JSONArray) obj;

            } catch (ParseException e) {
                e.printStackTrace();
            }

            return jsonObject;
        }
        else
            return new JSONArray();

    }


    public static JSONArray sendMethodsMetricsToDB(String json) {

        if (operationalBackend){
            Pair<Integer,String> res = sendJSONtoDB(greenRepoURL + methodsMetricsUrl, json);
            JSONParser parser = new JSONParser();
            JSONArray jsonObject = new JSONArray();
            if (res.first!=200) {
                System.out.println("FATAL ERROR ! HTTP CODE != 200 . something went wrong check internet connection or methods JSON FILE");
                return null;
            }
            try {
                Object obj = parser.parse(res.second);
                jsonObject = (JSONArray) obj;

            } catch (ParseException | ClassCastException e ) {
                e.printStackTrace();
            }

            return jsonObject;
        }
        else
            return new JSONArray();

    }


    public static JSONArray sendTestsMetricsToDB(String json) {
        if (operationalBackend){
            Pair<Integer,String> res = sendJSONtoDB(greenRepoURL + testsMetricsUrl, json);
            JSONParser parser = new JSONParser();
            JSONArray jsonObject = new JSONArray();
            if (res.first!=200) {
                System.out.println("FATAL ERROR ! HTTP CODE != 200 . something went wrong check internet connection or methods JSON FILE");
                return null;
            }
            try {
                Object obj = parser.parse(res.second);
                jsonObject = (JSONArray) obj;

            } catch (ParseException | ClassCastException e ) {
                e.printStackTrace();
            }

            return jsonObject;
        }
        else
            return new JSONArray();
    }




    public String getIDFromMethodInvokation(String methodID){
        for (Object jo : this.methodsInvoked.values()){
            JSONObject o = ((JSONObject) jo);
            if (o.containsKey("method")){
                if (o.get("method").equals(methodID)){
                    return o.get("id").toString();
                }
            }
        }
        return "null";
    }


    public static String generateMethodID(MethodInfo mi ){
        String args = "";
        for (Variable v : mi.args){
            args+=v.arrayCount+v.type+v.varName;
        }
        String metId= mi.ci.classPackage+"."+mi.ci.className+"."+ mi.methodName+"."+args.hashCode();
        return  metId;
    }

    public static JSONObject getMethodInvoked (String methodId, String testResID  ) {

        JSONObject test = new JSONObject();
        test.put("test_results", testResID);
        test.put("method", methodId);
        return test;
    }


    public static JSONObject sendMethodInvokedToDB(String json) {


        if(operationalBackend){
            Pair<Integer,String> res = sendJSONtoDB(greenRepoURL + methodsInvokedUrl, json);
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = new JSONObject();
            if (res.first!=200) {
                System.out.println("FATAL ERROR ! HTTP CODE != 200 . something went wrong check internet connection or methods JSON FILE");
                return null;
            }
            try {
                Object obj = parser.parse(res.second);
                jsonObject = (JSONObject) obj;

            } catch (ParseException | ClassCastException e ) {
                e.printStackTrace();
            }

            return jsonObject;
        }

        else return new JSONObject();
    }



    public static JSONArray sendMethodsInvokedToDB(String json) {

        if (operationalBackend){

            Pair<Integer,String> res = sendJSONtoDB(greenRepoURL + methodsInvokedUrl, json);
            JSONParser parser = new JSONParser();
            JSONArray jsonObject = new JSONArray();
            if (res.first!=200) {
                System.out.println("FATAL ERROR ! HTTP CODE != 200 . something went wrong check internet connection or methods JSON FILE");
                return null;
            }
            try {
                Object obj = parser.parse(res.second);
                jsonObject = (JSONArray) obj;

            } catch (ParseException | ClassCastException e ) {
                e.printStackTrace();
            }

            return jsonObject;
        }

        else{
            return  new JSONArray();
        }


    }

    public static JSONObject sendProjectToDB(String json) {
        if (operationalBackend){
            //System.out.println(greenRepoURL + projectsUlr);
            Pair<Integer,String> res = sendJSONtoDB(greenRepoURL + projectsUlr, json);
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = new JSONObject();
            if (res.first!=200) {
                System.out.println("FATAL ERROR ! HTTP CODE != 200 . something went wrong check internet connection or projects JSON FILE");
                return null;
            }
            try {
                Object obj = parser.parse(res.second);
                jsonObject = (JSONObject) obj;

            } catch (ParseException | ClassCastException e ) {
                e.printStackTrace();
            }

            return jsonObject;
        }

        else{
            return  new JSONObject();
        }


    }
    public static JSONArray sendClassesToDB(String json) {

        if (operationalBackend){
            Pair<Integer,String> res = sendJSONtoDB(greenRepoURL + classesUrl, json);
            JSONParser parser = new JSONParser();
            JSONArray jsonObject = new JSONArray();
            if (res.first!=200) {
                System.out.println("FATAL ERROR ! HTTP CODE != 200 . something went wrong check internet connection or methods JSON FILE");
                return null;
            }
            try {
                Object obj = parser.parse(res.second);
                jsonObject = (JSONArray) obj;

            } catch (ParseException | ClassCastException e ) {
                e.printStackTrace();
            }

            return jsonObject;
        }

        else{
            return  new JSONArray();
        }


    }

    public static JSONArray sendAppMetricsToDB(String json) {

        if (operationalBackend){
            Pair<Integer,String> res = sendJSONtoDB(greenRepoURL + appsMetricsUrl, json);
            JSONParser parser = new JSONParser();
            JSONArray jsonObject = new JSONArray();
            if (res.first!=200) {
                System.out.println("FATAL ERROR ! HTTP CODE != 200 . something went wrong check internet connection or methods JSON FILE");
                return null;
            }
            try {
                Object obj = parser.parse(res.second);
                jsonObject = (JSONArray) obj;

            } catch (ParseException | ClassCastException e ) {
                e.printStackTrace();
            }

            return jsonObject;
        }

        else{
            return  new JSONArray();
        }

    }

    public static JSONArray sendClassMetricsToDB(String json) {

        if (operationalBackend){
            Pair<Integer,String> res = sendJSONtoDB(greenRepoURL + classesMetricsUrl, json);
            JSONParser parser = new JSONParser();
            JSONArray jsonObject = new JSONArray();
            if (res.first!=200) {
                System.out.println("FATAL ERROR ! HTTP CODE != 200 . something went wrong check internet connection or methods JSON FILE");
                return null;
            }
            try {
                Object obj = parser.parse(res.second);
                jsonObject = (JSONArray) obj;

            } catch (ParseException | ClassCastException e ) {
                e.printStackTrace();
            }

            return jsonObject;
        }

        else{
            return  new JSONArray();
        }


    }


    public static JSONArray jsonObjToArray(JSONObject jo){
        JSONArray jas = new JSONArray();
        jas.addAll(jo.values());
        return jas;
    }




    public static JSONArray sendAppPermissionsToDB(String json) {


        if(operationalBackend){
            Pair<Integer,String> res = sendJSONtoDB(greenRepoURL + appPermissionsUrl, json);
            JSONParser parser = new JSONParser();
            JSONArray jsonObject = new JSONArray();
            if (res.first!=200) {
                System.out.println("FATAL ERROR ! HTTP CODE != 200 . something went wrong check internet connection or AppPermissions JSON FILE");
                return null;
            }
            try {
                Object obj = parser.parse(res.second);
                jsonObject = (JSONArray) obj;

            } catch (ParseException | ClassCastException e ) {
                e.printStackTrace();
            }
            return jsonObject;
        }
        else return new JSONArray();


    }

    public static JSONObject sendApplicationToDB(String json) {


        if(operationalBackend){
            Pair<Integer,String> res = sendJSONtoDB(greenRepoURL + appsUlr, json);
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = new JSONObject();
            if (res.first!=200) {
                System.out.println("FATAL ERROR ! HTTP CODE != 200 . something went wrong check internet connection or AppPermissions JSON FILE");
                return null;
            }
            try {
                Object obj = parser.parse(res.second);
                jsonObject = (JSONObject) obj;

            } catch (ParseException e) {
                e.printStackTrace();
            }


            return jsonObject;
        }
        else return new JSONObject();


    }

    public static JSONObject sendDeviceToDB(JSONObject jo) {
        jo.replace("device_max_cpu_freq", jo.getOrDefault(((String) jo.get("device_max_cpu_freq")).substring(0, Math.min( ((String) jo.get("device_max_cpu_freq")).length(), 16)), "")  );
        if(operationalBackend){
            Pair<Integer,String> res = sendJSONtoDB(greenRepoURL + devicesUrl, jo.toJSONString());
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = new JSONObject();
            if (res.first!=200) {
                System.out.println("FATAL ERROR ! HTTP CODE != 200 . something went wrong check internet connection or AppPermissions JSON FILE");
                return null;
            }
            try {
                Object obj = parser.parse(res.second);
                jsonObject = (JSONObject) obj;

            } catch (ParseException | ClassCastException e ) {
                e.printStackTrace();
            }

            if (!jsonObject.containsKey("device_serial_number")) {
                System.out.println("FATAL ERROR ! There is an error in test results JSON. Not submitting results do DBase");
                return null;

            } else return jsonObject;
        }
        else return new JSONObject();

    }

    public static int sendDeviceState( JSONObject jo) {
       if(operationalBackend){
            Pair<Integer,String> res = sendJSONtoDB(greenRepoURL + deviceStatesUrl, jo.toJSONString() );
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = new JSONObject();
            if (res.first!=200) {
                System.out.println("FATAL ERROR ! HTTP CODE != 200 . something went wrong check internet connection or AppPermissions JSON FILE");
                return -1;
            }
            try {
                Object obj = parser.parse(res.second);
                jsonObject = (JSONObject) obj;

            } catch (ParseException | ClassCastException e ) {
                e.printStackTrace();
            }

            if (!jsonObject.containsKey("state_id")) {
                System.out.println("FATAL ERROR ! There is an error in test results JSON. Not submitting results do DBase");
                return -1;

            }
            else return ((Long) jsonObject.get("state_id")).intValue();
        }
        else return -1;

    }

    public JSONArray getUniqueClassMetrics() {
        HashMap<String, JSONObject> uniqMap = new HashMap<>();
        this.classMetrics.forEach( x -> uniqMap.put(
                (((JSONObject) x).get("cm_class")==null? "" : (((JSONObject) x).get("cm_class").toString()))
                  + (((JSONObject) x).get("cm_metric")==null? "" : (((JSONObject) x).get("cm_metric").toString()))
                        + (((JSONObject) x).get("cm_value_text")==null? "" : (((JSONObject) x).get("cm_value_text").toString())), ((JSONObject) x)));
        JSONArray jas = new JSONArray();
        jas.addAll(uniqMap.values());
        return jas;
    }


    public static JSONObject loadLoginObject(String path){
        JSONObject object = Utils.loadJSONObj(path);
        if (object!=null && object.containsKey("username")){
            return object;
        }
        else {
            System.out.println("You must provide a valid login file in order to send data to GreenSource infrastructure");
            System.out.println("you should put in " + path +" a json object like { \"username\":\"example\", \"email\":\"example@mail.com\",\"password\":\"example\" } ");
            System.out.println("ignoring greensource URL. sending results to -> NONE");
            GreenSourceAPI.operationalBackend=false;
            return null;
        }
    }


    public static Pair<Integer,String> sendLoginJSONtoDB(String url, String JSONMessage) {
        CloseableHttpClient httpClient = HttpClientBuilder.create().disableCookieManagement().build();
        Integer httpRes = 0;
        String responseContent="";
        try {
            HttpPost request = new HttpPost(url);
            StringEntity params = new StringEntity(JSONMessage);
            request.addHeader("content-type", "application/json");
            request.setEntity(params);
            HttpResponse httpResponse = httpClient.execute(request);
            HttpEntity responseEntity = httpResponse.getEntity();
            httpRes = httpResponse.getStatusLine().getStatusCode();
            if (responseEntity != null) {
                responseContent = EntityUtils.toString(responseEntity);
            }

        } catch (Exception ex) {
            // handle exception here
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new Pair<Integer, String>(httpRes,responseContent);
    }



    private  static  BasicCookieStore setCookieStore ( List<Header> headerList){
        BasicCookieStore cookieStore = new BasicCookieStore();
        for ( Header h : headerList){
            String []full_cook = h.getValue().split("=");
            BasicClientCookie cookie = new BasicClientCookie(full_cook[0],  full_cook[1]);
            cookie.setDomain(greenRepoURL);
            cookie.setPath("/");
            cookieStore.addCookie(cookie);
        }
        return cookieStore;
    }

    public static Pair<Integer,String> sendJSONtoDB(String url, String JSONMessage) {
        //CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultCookieStore(setCookieStore(headerList)).build();
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        Integer httpRes = 0;
        String responseContent="";
        try {
            HttpPost request = new HttpPost(url);
            StringEntity params = new StringEntity(JSONMessage);
            request.addHeader("content-type", "application/json");
            //  headerList.forEach( x -> request.addHeader( x)  );
            request.addHeader("Authorization", "Token " + JWTtoken );
            request.setEntity(params);
            HttpResponse httpResponse = httpClient.execute(request);
            //Arrays.stream(httpResponse.getAllHeaders()).filter(x -> x.getName().equals("Set-Cookie") ).forEach(z -> headerList.add(z));
            HttpEntity responseEntity = httpResponse.getEntity();
            httpRes = httpResponse.getStatusLine().getStatusCode();
            if (responseEntity != null) {
                responseContent = EntityUtils.toString(responseEntity);
            }

        } catch (Exception ex) {
            // handle exception here
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new Pair<Integer, String>(httpRes,responseContent);
    }

    public void loginGreenSource (String  loginJSONPath){
        JSONObject  jo = loadLoginObject(loginJSONPath);
        try {
            sendLoginToGSource(jo);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void sendLoginToGSource(JSONObject loginJSON) throws ParseException {
        if (operationalBackend){
            Pair<Integer,String> res = sendLoginJSONtoDB(greenRepoURL + loginURL, loginJSON.toJSONString());
            if (res.first==200){
                JSONParser parser = new JSONParser();
                JSONObject json = (JSONObject) parser.parse(res.second);
                JWTtoken =  json.get("key").toString();
            }
            else {
                System.out.println("Login refused. Please register the current user in GreenSource first ( <actual_url>/register )");
                System.out.println("ignoring greensource URL. sending results to -> NONE");
                GreenSourceAPI.operationalBackend=false;
            }
        }
    }
}
