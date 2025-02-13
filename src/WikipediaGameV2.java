import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class WikipediaGameV2 {
    public WikipediaGameV2() {
        String startTitle = "Ed Sheeran";
        String goalTitle = "Philosophy";
        WikipediaPage currentPage;
        ArrayList<Path> queue = new ArrayList<>();
        WikipediaPage startPage = new WikipediaPage(startTitle,"");
        startPage.findChildren(goalTitle);
        for(Path p: startPage.children){
            queue.addLast(p);
        }
        while(!queue.isEmpty()){
            currentPage = new WikipediaPage(queue.getFirst().title,queue.getFirst().path);
            currentPage.findChildren(goalTitle);
            for(Path p: currentPage.children){
                queue.addLast(p);
            }
            queue.removeFirst();
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
    ArrayList<Path> children = new ArrayList<>();
    boolean childrenFound = false;
    String pathString = "";
    public WikipediaPage(String title,String previousPath) {
        this.title = title;
        this.titleUsingUnderscores = underscorify(title);
        if(previousPath.equals("")){
            pathString = title;
        } else {
            pathString += (previousPath + " -> " + title);
        }
    }
    void findChildren(String goal){
        json=importJSON("https://en.wikipedia.org/w/api.php?action=query&format=json&prop=links&titles="+titleUsingUnderscores+"&formatversion=2&pllimit=max");
        if(json==null){
            childrenFound=true;
        } else {
            getChildrenfromtheJSON(goal);
            if (json.containsKey("batchcomplete")) {
                childrenFound = true;
            } else {
                JSONObject overArchingContinueObject = (JSONObject) json.get("continue");
                getRestOfChildren((String) overArchingContinueObject.get("plcontinue"), goal);
            }
        }
    }
    void getRestOfChildren(String plcontinue,String goal){

        this.json = importJSON(("https://en.wikipedia.org/w/api.php?action=query&format=json&prop=links&titles="+titleUsingUnderscores+"&formatversion=2&pllimit=max&plcontinue="+urlVersionOfPLContinue(plcontinue)));
        if(json!=null) {
            getChildrenfromtheJSON(goal);
            if (json.containsKey("batchcomplete")) {
                childrenFound = true;
            } else {
                JSONObject overArchingContinueObject = (JSONObject) json.get("continue");
                getRestOfChildren(((String) (overArchingContinueObject.get("plcontinue"))), goal);
            }
        }
    }

    void getChildrenfromtheJSON(String goal){
        if(!(json==null)) {
            JSONObject query = (JSONObject) json.get("query");
            JSONArray pages = (JSONArray) query.get("pages");
            JSONObject content = (JSONObject) pages.getFirst();
            JSONArray links = (JSONArray) content.get("links");
            if(links!=null) {
                for (int x = 0; x < links.size(); x++) {
                    JSONObject objectInArray = (JSONObject) links.get(x);
                    String title = (String) objectInArray.get("title");
                    children.add(new Path(title, this.pathString));
                    System.out.println(title);
                    if (title.equals(goal)) {
                        System.out.println("PATH FOUND: " + pathString + " -> " + title);
                        System.exit(0);
                    }
                }
            }
        }
    }

    String urlVersionOfPLContinue(String plcontinue){
        int indexOfFirstPipe = plcontinue.indexOf('|');
        int indexOfSecondPipe = plcontinue.indexOf('|',indexOfFirstPipe+1);
        return plcontinue.substring(0,indexOfFirstPipe)+"%7C"+plcontinue.substring(indexOfFirstPipe+1,indexOfSecondPipe)+"%7C"+plcontinue.substring(indexOfSecondPipe+1);
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


            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));


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
class Path{
    String title;
    String path; //this goes until the one right before the title.
    public Path(String title, String path) {
        this.title = title;
        this.path = path;
    }
}
