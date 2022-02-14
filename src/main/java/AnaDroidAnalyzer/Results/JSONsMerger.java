package AnaDroidAnalyzer.Results;


import AnaDroidAnalyzer.Analyzer.MainAnalyzer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import static AnaDroidAnalyzer.Utils.Utils.*;

/*
* Class to merge allMethods.json and the json resultant from apk analysis
* */
public class JSONsMerger {

    private   JSONObject apkMethods = new JSONObject();
    private JSONObject sourceMethods = new JSONObject();
    private JSONObject merged= new JSONObject();
    private Set<JSONObject> ignoredMethods=  new HashSet();
    private int apkMethodsCount=  -1;

    public JSONObject getSourceMethods(){
        return sourceMethods;
    }

    public JSONObject getMerged() {
        return merged;
    }

    public JSONObject getApkMethods() {
        return apkMethods;
    }

    public int getApkMethodCoverage(){
        if (apkMethodsCount>=0){
            return apkMethodsCount;
        }
        else {
            int method_count = 0;
            for (Object o : apkMethods.values()) {
                JSONObject jo = ((JSONObject) o);
                if (jo.containsKey("class_methods")){
                    method_count += ((JSONObject) jo.get("class_methods")).size();
                }
            }
            apkMethodsCount = method_count;
            return method_count;
        }
    }

    public int getSourceCoverage(){
       return merged.size();
    }

    public JSONsMerger() {
    }

    public JSONsMerger(String apkMethodsFile, String sourceMethodsFile) {
        this.apkMethods = loadJSONObj(apkMethodsFile);
        this.sourceMethods = loadJSONObj(sourceMethodsFile);
        this.merged = new JSONObject();
    }


    public void merge( ){
        String separator = "->";
        String separator_of_hash = "|";
        if (sourceMethods.isEmpty()){
            for (Object apkobj : apkMethods.values()){
                JSONObject apko= ((JSONObject) apkobj);
                if (apko.containsKey("class_methods")){
                    merged.putAll((JSONObject) apko.get("class_methods"));
                }
            }
        }
        else {
            Iterator it = sourceMethods.keySet().iterator();
            while (it.hasNext()) {
                String source_method_key = ((String) it.next());
                JSONObject source_method_obj = ((JSONObject) (sourceMethods.get(source_method_key)));
                String source_method_class = source_method_key.split("->")[0];
                String source_method_simple_name = source_method_key.split("->")[1].split("\\" + separator_of_hash)[0].replace("clinit", "init");
                // primeiro tentar ir buscar diretamente a classe
                JSONObject correspondant_apk_class = ((JSONObject) apkMethods.get(source_method_class));
                if (correspondant_apk_class == null || !correspondant_apk_class.containsKey("class_methods")) {
                    //System.out.println("SEVERE ERROR obtaining correspondant class");
                    ignoredMethods.add(source_method_obj);
                    continue;
                } else {
                    JSONObject methods_of_class = ((JSONObject) correspondant_apk_class.get("class_methods"));
                    Set<String> possible_methods = (Set<String>) methods_of_class.keySet().stream().filter(x -> ((String) x).contains(source_method_simple_name)).map(x -> ((String) x).toString()).collect(Collectors.toSet());
                    if (possible_methods.isEmpty()) {
                        // ignore method
                        ignoredMethods.add(source_method_obj);
                    } else if (possible_methods.size() == 1) {
                        foundMerged(source_method_key, ((JSONObject) methods_of_class.get(possible_methods.toArray()[0])), source_method_obj);
                        it.remove();
                    } else { // try to find match
                        HashSet<JSONObject> possibleAPKObjs = new HashSet<>();
                        possible_methods.forEach(x -> possibleAPKObjs.add(((JSONObject) methods_of_class.get(x))));
                        JSONArray source_method_args = ((JSONArray) source_method_obj.get("args"));
                        int possibleMatches = possibleAPKObjs.size();
                        for (JSONObject possible : possibleAPKObjs) {
                            boolean equals = true;
                            int possible_apk_nr_args = ((JSONArray) possible.get("method_args")).size();
                            if (possible_apk_nr_args == source_method_args.size()) {
                                for (int i = 0; (i < ((JSONArray) possible.get("method_args")).size()); i++) {
                                    String apkarg = ((String) ((JSONArray) possible.get("method_args")).get(i)).replaceAll("\\[", "");
                                    String arg = ((String) source_method_args.get(i)).replaceAll("\\[", "");
                                    if (!apkarg.toLowerCase().endsWith(arg.toLowerCase()) && !apkarg.toLowerCase().replaceAll("$", "").endsWith(arg.toLowerCase())) {
                                        equals = false;
                                        possibleMatches--;
                                        break;
                                    }
                                }
                                if (equals) {
                                    foundMerged(source_method_key, possible, source_method_obj);
                                    // System.out.println(  source_method_key + " matched with " + possible );
                                }
                            } else {
                                possibleMatches--;
                            }
                        }
                        if (possibleMatches <= 0) {
                            //System.out.println(" checka este ->" + source_method_key);
                            ignoredMethods.add(source_method_obj);
                        }
                    }
                }
            }

        }
        for (JSONObject ignored : ignoredMethods){
            ignored.put("method_apis",  new JSONArray());
            ignored.put("method_nr_instructions", -1);
            ignored.put("method_locals", -1);
            ignored.put("method_length", -1);
            ignored.put("method_name", ((String) ignored.get("name")));
            ignored.put("method_class", ((String) ignored.get("name")).split("->")[0]);
            ignored.remove(((String) ignored.get("name")));
            ignored.put("method_args", ignored.get("args") );
            ignored.remove("args");
            merged.put(ignored.get("name"),ignored);
        }
    }

/*
    public void merge(){x
        merge2();
        String separator = "->";
        String separator_of_hash = "|";
        Iterator it = apkMethods.values().iterator();
        while (it.hasNext()){
            JSONObject classObj = ((JSONObject) it.next());
            if ( ! classObj.containsKey("class_methods")){
                continue;
            }
            JSONObject methods_of_class = ((JSONObject) classObj.get("class_methods"));
            for (Object method_obj : methods_of_class.keySet()){
                JSONObject method_apk_json_obj = ((JSONObject) methods_of_class.get(method_obj));
                String method_apk_key = ((String) method_apk_json_obj.get("method_name"));
                StringBuilder sb = new StringBuilder();
                JSONArray method_apk_args = ((JSONArray) ((JSONObject) methods_of_class.get(method_obj)).get("method_args"));
                for (Object oak : method_apk_args){
                    String [] splited_arg = ((String) oak).split("\\.");
                    sb.append(splited_arg[splited_arg.length-1]);
                }
                int hashedArgs = sb.toString().toLowerCase().hashCode();
                String search_method_string = method_apk_key + separator_of_hash + String.valueOf(hashedArgs);
                if (sourceMethods.containsKey(search_method_string)){
                    foundMerged(search_method_string,(JSONObject) methods_of_class.get(method_obj), ((JSONObject) sourceMethods.get(search_method_string)));
                    classObj.put("class_language", ((JSONObject) sourceMethods.get(search_method_string)));
                }
                else{
                    // get source Methods of source class
                    String apk_method_class = method_apk_key.split(separator)[0];
                    String apk_method_simple_name = method_apk_key.split(separator)[1].replace("clinit", "init");
                    Set<String> possible_methods =  (Set<String>) sourceMethods.keySet().stream().filter(x-> ((String) x).contains(apk_method_class) && ((String) x).contains(apk_method_simple_name) ).map(x -> ((String) x).toString()).collect(Collectors.toSet());
                    if (possible_methods.isEmpty()){
                        // ignore method
                        ignoredMethods.put(method_apk_key, method_apk_json_obj);
                    }
                    else if (possible_methods.size()==1){
                        foundMerged(((String) ((JSONObject) sourceMethods.get(possible_methods.toArray()[0])).get("name")),(JSONObject) methods_of_class.get(method_obj),((JSONObject) sourceMethods.get(possible_methods.toArray()[0]) ));
                        classObj.put("class_language", ((JSONObject) sourceMethods.get(possible_methods.toArray()[0])).get("language"));
                    }
                    else{ // try to find match
                        HashSet<JSONObject> possibleSourceMatches = new HashSet<>();
                        possible_methods.forEach( x -> possibleSourceMatches.add(((JSONObject) sourceMethods.get(x))));
                        for ( JSONObject possible : possibleSourceMatches){
                            boolean equals = true;
                            int possible_nr_args = ((JSONArray) possible.get("args")).size();
                            if (possible_nr_args == method_apk_args.size()) {
                                for (int i = 0; (i < ((JSONArray) possible.get("args")).size()) ; i++) {
                                    String arg = ((String) ((JSONArray) possible.get("args")).get(i));
                                    String apkarg = ((String) method_apk_args.get(i));
                                    if (! apkarg.endsWith(arg) && !apkarg.replaceAll("$", "").endsWith(arg)) {
                                        equals = false;
                                        break;
                                    }
                                }
                                if (equals){
                                    foundMerged(((String) possible.get("name")),(JSONObject) methods_of_class.get(method_obj), possible);
                                    classObj.put("class_language", possible.get("language"));
                                    System.out.println(  method_apk_json_obj + " matched with " + possible );
                                }
                            }

                        }
                    }
                }
            }
        }
        System.out.println("merged methods: " + merged.size());
    }

*/





    private void foundMerged(String key,JSONObject apk_obj, JSONObject source_obj){
        JSONObject mergedObj = new JSONObject();
        mergedObj.putAll(apk_obj);
        mergedObj.put("method_name", key.split("->")[1].split("\\|")[0]);
        mergedObj.put("method_hash",((String) source_obj.get("hash")));
        apk_obj.put("method_hash",((String) source_obj.get("hash")));

        merged.put(key, mergedObj);
    }
    

    /*
    public void merge(){
        String separator = "->";
        Iterator it = sourceMethods.keySet().iterator();
        //System.out.println("Source methods: " + sourceMethods.size());
        //int debug =0;
        while (it.hasNext()){
            String key = ((String) it.next());
            JSONObject jo = ((JSONObject) sourceMethods.get(key));
            String method_name_with_hash = key.split(separator)[key.split(separator).length-1];
            String method_name = method_name_with_hash.split("\\|")[0];
            String hash = ((String) jo.get("hash"));
            String fullClassName = key.split("->")[0];
            String simple_ClassName = fullClassName.split("\\.")[fullClassName.split("\\.").length-1];
            JSONArray  method_args = ((JSONArray) jo.get("args"));
            for (Object oo : apkMethods.values() ) {
                JSONObject classObj = ((JSONObject) oo);
                if ( ! classObj.containsKey("class_methods")){
                    continue;
                }
                for (Object ooo : ((JSONObject) classObj.get("class_methods")).values()) {
                    JSONObject apkObj = ((JSONObject) ooo);
                    String m_key = ((String) apkObj.get("method_name"));
                    JSONArray apk_method_args = ((JSONArray) apkObj.get("method_args"));
                    String apk_method_name = m_key.split(separator)[m_key.split(separator).length - 1];
                    if(key.contains("Image") && key.contains("init")){
                        System.out.println();
                    }
                    if (apk_method_name.equals(method_name) && m_key.contains(simple_ClassName) && apk_method_args.size() == method_args.size()  ) {
                        boolean equals = true;
                        //debug++;
                        //  at this point we know they have the same arg len and name
                        StringBuilder sb = new StringBuilder();
                        for (Object oak : ((JSONArray) apk_method_args)){
                            String [] splited_arg = ((String) oak).split("\\.");
                            sb.append(splited_arg[splited_arg.length-1]);
                        }
                        int hashedArgs = sb.toString().toLowerCase().hashCode();
                        int source_hash = Integer.parseInt ((String) jo.get("hash"));
                        for (int i = 0; (i < method_args.size()) && ( hashedArgs != source_hash); i++) {
                            String arg = ((String) method_args.get(i));
                            String apkarg = ((String) apk_method_args.get(i));
                            if (!apkarg.endsWith(arg) && !apkarg.replaceAll("$", "").endsWith(arg)) {
                                equals = false;
                                break;
                            }
                        }
                        if (equals) {
                            JSONObject mergedObj = new JSONObject();
                            mergedObj.putAll(apkObj);
                            mergedObj.put("language", jo.get("language"));
                            mergedObj.put("method_hash",hash);
                            apkObj.put("method_hash",hash);
                            classObj.put("class_language", jo.get("language"));
                            merged.put(key, mergedObj);
                            try {
                                it.remove();
                            }
                            catch (IllegalStateException e){
                            }
                            break;
                        }
                        else {
                        }
                    }
                }
            }
        }
        //unmatched methods
        for (Object sourceMethod : sourceMethods.values()) {
            JSONObject ja = ((JSONObject) sourceMethod);
            JSONArray jas = new JSONArray();
            String full_method_name = ((String) ja.get("name"));
            String classNameOfMethod = full_method_name.split("->")[0];
            if(apkMethods.containsKey(classNameOfMethod)){
                JSONObject jo = ((JSONObject) ((JSONObject) apkMethods.get(classNameOfMethod)).get("class_methods"));
                String method_simpler_name = full_method_name.split("\\|")[0];
                if (jo.containsKey(method_simpler_name)){
                    JSONObject correspondant_method = ((JSONObject) jo.get(method_simpler_name));
                    ja.put("apis", ((JSONArray) correspondant_method.get("method_apis")));
                    ja.put("return", correspondant_method.get("method_return"));
                    merged.put(ja.get("name"), ja);
                }
            }
            else{
                jas.add("UnknownAPIS");
                ja.put("apis", jas);
                ja.put("return", "UnknownReturn");
                merged.put(ja.get("name"), ja);
            }

        }
        System.out.println("APK methods: " + apkMethods.size());

    }

*/





}
