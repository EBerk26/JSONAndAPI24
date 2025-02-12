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

public class WikipediaGameFirstDraft {
    public static void main(String[] args) {
        WikipediaPageFirstDraft joe = new WikipediaPageFirstDraft("2016 Toronto International Film Festival");
        joe.findChildren();
    }
    public WikipediaGameFirstDraft() {
    }

}
class WikipediaPageFirstDraft {
    String numberOfLinksAway = "max"; //perhaps I can write some sort of algorithm to determine the most efficient number for these
    String numberOfLinksHere = numberOfLinksAway;
    String title;
    ArrayList<WikipediaPageFirstDraft> parents = new ArrayList<>();
    ArrayList<WikipediaPageFirstDraft> children = new ArrayList<>();
    boolean parentsFound = false;
    boolean childrenFound=false;
    JSONObject json;
    WikipediaPageFirstDraft(String title){
        System.out.println(title);
        String titleUsingUnderscores = underscorify(title);
        json = importJSON("https://en.wikipedia.org/w/api.php?action=query&format=json&prop=links%7Clinkshere&list=&titles="+titleUsingUnderscores+"+&formatversion=2&pllimit="+numberOfLinksAway+"&lhprop=title&lhlimit="+numberOfLinksHere);
    }
    void findParents(){
        JSONObject query = (JSONObject) json.get("query");
        JSONArray pages = (JSONArray) query.get("pages");
        JSONObject content = (JSONObject) pages.getFirst();
        JSONArray links = (JSONArray) content.get("linkshere");
        for(int x =0;x<links.size();x++){
            JSONObject objectInArray = (JSONObject)links.get(x);
            parents.add(new WikipediaPageFirstDraft((String)objectInArray.get("title")));
        }

        //query (obj)- value (obj) - pages (array) - value (array) -

        parentsFound = true;
    }
    void findChildren(){
        JSONObject query = (JSONObject) json.get("query");
        JSONArray pages = (JSONArray) query.get("pages");
        JSONObject content = (JSONObject) pages.get(0);
        JSONArray links = (JSONArray) content.get("links");
        System.out.println();
        for(int x =0;x<links.size();x++){
            JSONObject objectInArray = (JSONObject)links.get(x);
            parents.add(new WikipediaPageFirstDraft((String)objectInArray.get("title")));
        }

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
    String underscorify(String noUnderscores){
        String noUnderscoresEditable = noUnderscores;
        while(noUnderscoresEditable.contains(" ")){
            int indexOfSpace = noUnderscoresEditable.indexOf(" ");
            noUnderscoresEditable=noUnderscoresEditable.substring(0,indexOfSpace)+"_"+noUnderscoresEditable.substring(indexOfSpace+1,noUnderscoresEditable.length());
        }

        return noUnderscoresEditable;
    }
}
