import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class WikipediaGameV2 {
    public WikipediaGameV2() {
        String startPage = "Glen Powell";
        String goalPage = "Kevin Bacon";
        ArrayList<WikipediaPage> pages = new ArrayList<>();
        WikipediaPage joe = new WikipediaPage(startPage,"");
        joe.findChildren(goalPage);

        //rudimentary pathfinding for stuff with a distance of 2
        if(joe.childrenFound){
            for(String s:joe.children){
                pages.add (new WikipediaPage(s,joe.path));
                pages.getLast().findChildren(goalPage);
            }
        }
    }
    public static void main(String[] args) {
        new WikipediaGameV2();
    }
}

class WikipediaPage {
    String title;
    String titleUsingUnderscores;
    JSONObject json;
    ArrayList<String> children = new ArrayList<>();
    boolean childrenFound = false;
    String path = "";
    public WikipediaPage(String title,String previousPath) {
        this.title = title;
        this.titleUsingUnderscores = underscorify(title);
        if(path.equals("")){
            path = title;
        } else {
            path += (previousPath + " -> " + title);
        }
    }
    void findChildren(String goal){
        json=importJSON("https://en.wikipedia.org/w/api.php?action=query&format=json&prop=links&titles="+titleUsingUnderscores+"&formatversion=2&pllimit=max");
        getChildrenfromtheJSON(goal);
        if(json.containsKey("batchcomplete")){
            childrenFound = true;
        } else {
            JSONObject overArchingContinueObject = (JSONObject)json.get("continue");
            getRestOfChildren((String)overArchingContinueObject.get("plcontinue"),goal);
        }
    }
    void getRestOfChildren(String plcontinue,String goal){
        this.json = importJSON("https://en.wikipedia.org/w/api.php?action=query&format=json&prop=links&titles="+titleUsingUnderscores+"&formatversion=2&pllimit=max&plcontinue="+urlVersionOfPLContinue(plcontinue));
        getChildrenfromtheJSON(goal);
        if(json.containsKey("batchcomplete")){
            childrenFound = true;
        } else {
            System.out.println();
            JSONObject overArchingContinueObject = (JSONObject)json.get("continue");
            System.out.println();
            getRestOfChildren((String)(overArchingContinueObject.get("plcontinue")),goal);
        }
    }

    void getChildrenfromtheJSON(String goal){
        JSONObject query = (JSONObject) json.get("query");
        JSONArray pages = (JSONArray) query.get("pages");
        JSONObject content = (JSONObject) pages.getFirst();
        JSONArray links = (JSONArray) content.get("links");
        System.out.println();
        for(int x =0;x<links.size();x++){
            JSONObject objectInArray = (JSONObject)links.get(x);
            String title = (String)objectInArray.get("title");
            children.add(title);
            System.out.println(title);
            if(title.equals(goal)){
                System.out.println("PATH FOUND: "+path+" -> "+title);
                System.exit(0);
            }
        }
    }

    String urlVersionOfPLContinue(String plcontinue){
        int indexOfFirstPipe = plcontinue.indexOf('|');
        return plcontinue.substring(0,indexOfFirstPipe)+"%7C0%7C"+plcontinue.substring(indexOfFirstPipe+3);
    }

    String underscorify(String noUnderscores){
        String noUnderscoresEditable = noUnderscores;
        while(noUnderscoresEditable.contains(" ")){
            int indexOfSpace = noUnderscoresEditable.indexOf(" ");
            noUnderscoresEditable=noUnderscoresEditable.substring(0,indexOfSpace)+"_"+noUnderscoresEditable.substring(indexOfSpace+1);
        }

        return noUnderscoresEditable;
    }
    JSONObject importJSON(String link){
        String output;
        StringBuilder jsonString= new StringBuilder();
        try {

            URL url = new URL(link); // Your API's URL goes here
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");


            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));


            while ((output = br.readLine()) != null) {
                jsonString.append(output);
            }

            conn.disconnect();
            JSONParser parser = new JSONParser();
            return (JSONObject) parser.parse(jsonString.toString());

        } catch (ParseException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
