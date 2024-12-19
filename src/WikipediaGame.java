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
import java.util.Objects;

public class WikipediaGame {
    public static void main(String[] args) {
        WikipediaPage joe = new WikipediaPage("2016 Toronto International Film Festival");
        joe.findParents();
    }
    public WikipediaGame() {
    }

}
class WikipediaPage{
    String title;
    ArrayList<WikipediaPage> parents = new ArrayList<>();
    ArrayList<WikipediaPage> children = new ArrayList<>();
    boolean parentsFound = false;
    boolean childrenFound=false;
    JSONObject json;
    WikipediaPage(String title){
        System.out.println(title);
        String titleUsingUnderscores = title;
        //replace spaces with underscores
        while(titleUsingUnderscores.contains(" ")){
            int indexOfSpace = titleUsingUnderscores.indexOf(" ");
            titleUsingUnderscores=titleUsingUnderscores.substring(0,indexOfSpace)+"_"+titleUsingUnderscores.substring(indexOfSpace+1,titleUsingUnderscores.length());
        }
        json = importJSON("https://en.wikipedia.org/w/api.php?action=query&format=json&prop=links%7Clinkshere&list=&titles="+titleUsingUnderscores+"+&formatversion=2&pllimit=max&lhprop=title%7Credirect%7Cpageid&lhlimit=max");
    }
    void findParents(){
        JSONObject query = (JSONObject) json.get("query");
        JSONArray pages = (JSONArray) query.get("pages");
        JSONObject content = (JSONObject) pages.get(0);
        JSONArray links = (JSONArray) content.get("linkshere");
        for(int x =0;x<links.size();x++){
            JSONObject objectInArray = (JSONObject)links.get(x);
            parents.add(new WikipediaPage((String)objectInArray.get("title")));
        }

        //query (obj)- value (obj) - pages (array) - value (array) -

        parentsFound = true;
    }
    void findChildren(){

    }
    JSONObject importJSON(String link){
        if(link.contains("Pablo")){
            System.out.println();
        }
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
