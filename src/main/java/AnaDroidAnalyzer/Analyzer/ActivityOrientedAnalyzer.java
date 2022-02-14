package AnaDroidAnalyzer.Analyzer;

import AnaDroidAnalyzer.Results.TestResults;
import AnaDroidAnalyzer.Results.TrepnResults;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static AnaDroidAnalyzer.Utils.Utils.loadJSONObj;

public class ActivityOrientedAnalyzer extends TestOrientedAnalyzer {

    @Override
    public  double setInvokedMethodsAndmethodCoverageTestOriented(TrepnResults actualResult, Path pathTraced){
        String realPath=  pathTraced.toAbsolutePath().toString().replace(".txt",".json");
        HashMap<String, Integer> thisTest = new HashMap<>();
        JSONObject jo = loadJSONObj(realPath);
        for (Object objarray : jo.values()) {
            JSONArray jas = ((JSONArray) objarray);
            for (Object o : jas) {
                JSONObject methodjson = ((JSONObject) o);
                String key = methodjson.get("class") + "->" + methodjson.get("name");
                Set<String> possible_methods = (Set<String>) allmethods.getMerged().keySet().stream().filter(x -> ((String) x).contains(key)).map(x -> ((String) x)).collect(Collectors.toSet());
                if (possible_methods.isEmpty()) {
                    // ignore method
                    System.out.println("Warning: Unmatched method -> " + key );
                    System.out.println("This method was invoked during app execution, but the identifier doesn't match any of" +
                            "identifiers gathered during isntrumentation phase");
                    continue;
                } else if (possible_methods.size() == 1) {
                    String m_id = ((String) possible_methods.toArray()[0]);
                    if (thisTest.containsKey(m_id)) {
                        int x = thisTest.get(m_id);
                        thisTest.put(m_id, ++x);
                        actualTestMethods.add(m_id);
                    } else {
                        thisTest.put(m_id, 1);
                        actualTestMethods.add(m_id);
                    }
                } else { // try to find match
                    HashSet<JSONObject> possibleAPKObjs = new HashSet<>();
                    possible_methods.forEach(x -> possibleAPKObjs.add(((JSONObject) allmethods.getMerged().get(x))));
                    JSONArray source_method_args = ((JSONArray) methodjson.get("args"));
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
                                String m_id = ((String) possible.get("method_name"));
                                if (thisTest.containsKey(m_id)) {
                                    int x = thisTest.get(m_id);
                                    thisTest.put(m_id, ++x);
                                    actualTestMethods.add(m_id);
                                } else {
                                    thisTest.put(m_id, 1);
                                    actualTestMethods.add(m_id);
                                }
                            }
                        } else {
                            possibleMatches--;
                        }
                    }
                }
            }
        }
        ((TestResults) actualResult).invokedMethods = thisTest;
        // add 1 because it lacks on Create meethod
        double percentageCoverage = ((double) (thisTest.size() +1)/(double)(allmethods.getSourceCoverage()));
        return percentageCoverage;
    }

}
