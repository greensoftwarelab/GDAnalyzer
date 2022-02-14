import AnaDroidAnalyzer.GreenSourceBridge.GreenSourceAPI;
import AnaDroidAnalyzer.Results.JSONsMerger;
import AnaDroidAnalyzer.Utils.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeout;

public class GeneralTests {

    @Test
    void add() {
        String sourceM = "/Users/ruirua/GDResults/x1125io.initdlight_2_src.tar2.gz/all/allMethods.json";
        String apkM = "/Users/ruirua/GDResults/x1125io.initdlight_2_src.tar2.gz/all/x1125io.initdlight.json";
        //String apkM =  "/Users/ruirua/repos/AnaDroid/uminho.di.greenlab.n2apptest.json";
        JSONsMerger jm = new JSONsMerger(apkM, sourceM);
        jm.merge();
        jm.getMerged().values().forEach(it -> System.out.println(it));
    }



    @Test
    void sendProjects() {
        GreenSourceAPI gap = new GreenSourceAPI();
        gap.setGreenRepoUrl("http://greensource.di.uminho.pt/");
        //gap.setGreenRepoUrl("http://localhost:8000/");
        String sourceProjects = "/Users/ruirua/repos/greenSource/fDroid_extractor/fdroid_apps.json";
        JSONArray toSend = new JSONArray();
        JSONObject target = new JSONObject();
        JSONArray jas = Utils.loadJSONArray(sourceProjects);
        int i =0;
        for (Object o : jas){
            target = new JSONObject();
            JSONObject jo = ((JSONObject) o);
            System.out.println(jo.get("url"));
            if (jo.get("url")!=null){
                String pack = jo.get("url").toString().replace("https://f-droid.org/repo/", "").replace("_src.tar.gz","");
                if (pack.matches("(.*|\\.)*_[0-9]+")){
                    String realPack = pack.split("_")[0];
                    System.out.println(realPack);
                    target.put("project_id", realPack.hashCode());
                }
                else {
                    target.put("project_id", pack.hashCode());
                }
                if (jo.get("description")!=null){
                    ;
                    target.put("project_desc", jo.get("description").toString().length()>512?  jo.get("description").toString().substring(0,511): jo.get("description"));
                }
                else {
                    target.put("project_desc", jo.get("summary").toString().length()>512?  jo.get("summary").toString().substring(0,511): jo.get("summary"));
                }
                target.put("project_build_tool", "gradle?");
                target.put("project_location", new String(((String) jo.get("url")).getBytes(UTF_8) , UTF_8 ) );
                toSend.add(target);
            }
        }
        JSONObject auth = new JSONObject();
        auth.put("username", "root");
        auth.put("email", "rui.rrua@gmail.com");
        auth.put("password", "greenroot8");
        try {
            gap.sendLoginToGSource(auth);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        toSend.forEach( x -> gap.sendProjectToDB(((JSONObject) x).toString()));
        System.out.println(toSend);
    }

    @Test
    void sendOldProjects() {
        GreenSourceAPI gap = new GreenSourceAPI();
        gap.setGreenRepoUrl("http://greensource.di.uminho.pt/");
        List<String> l = new ArrayList<>();
        try {
            l = java.nio.file.Files.readAllLines(Paths.get("/Users/ruirua/repos/greenSource/old_ids.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONArray jas = new JSONArray();
        for ( String s : l){
            String id  = s.replace("/data/","").replace(".zip","");
            JSONObject target = new JSONObject();
            target.put("project_id", id);
            target.put("project_build_tool", "gradle");
            target.put("project_desc", "");
            target.put("project_location", "http://greensource.di.uminho.pt/" + id + ".zip");
            jas.add(target);
        }
        JSONObject auth = new JSONObject();
        auth.put("username", "root");
        auth.put("email", "rui.rrua@gmail.com");
        auth.put("password", "greenroot8");
        try {
            gap.sendLoginToGSource(auth);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        jas.forEach( x -> gap.sendProjectToDB(((JSONObject) x).toString()));
        System.out.println(jas);

    }



}