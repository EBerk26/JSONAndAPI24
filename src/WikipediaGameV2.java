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
    public String findPath(String startTitle, String goalTitle) {
        WikipediaPage currentPage;
        ArrayList<Path> queue = new ArrayList<>();
        ArrayList<Path> goalPages = new ArrayList<>();
        WikipediaPage startPage = new WikipediaPage(startTitle,"",false);
        WikipediaPage endPage = new WikipediaPage(goalTitle,"",true);
        /*endPage.findParents();
        for(Path p: endPage.children){
            goalPages.addLast(p);
        }
        while(goalPages.size()<=2000){
            //find goal pages until you have 2k of them or however many
        }*/
        startPage.findChildren(goalTitle);

        for(Path p: startPage.children){
          queue.addLast(p);
        }
        while(!queue.isEmpty()) {
            currentPage = new WikipediaPage(queue.getFirst().title, queue.getFirst().path, false);
            String search = currentPage.findChildren(goalTitle);
            if (search != null) {
                return search;
            } else {
                for (Path p : currentPage.children) {
                    if (p.title.contains(goalTitle) || p.title.contains(goalTitle.toLowerCase())) { //if the previous title is contained somewhere it is prioritized.
                        queue.addFirst(p);
                    } else {
                        queue.addLast(p);
                    }
                }
                queue.removeFirst();
            }
        }
        return "failure";
    }
    public WikipediaGameV2(boolean test){
        WikipediaPage endPage = new WikipediaPage("Taylor Swift","",true);
        endPage.findParents();
    }
    public WikipediaGameV2(){
        System.out.println(findPath("Taylor Swift","Noah Kahan"));
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
    boolean parentsFound = true;
    ArrayList<Path> parents = new ArrayList<>();
    String pathString = "";
    public WikipediaPage(String title,String previousPath,boolean backTracking) {
        this.title = title;
        this.titleUsingUnderscores = underscorify(title);
        if(backTracking){
            if(previousPath.equals("")){
                pathString = this.title;
            } else {
                pathString = this.title + " -> " + pathString;
            }
        } else if(previousPath.equals("")){
            pathString = title;
        } else {
            pathString += (previousPath + " -> " + title);
        }


    }
    String findChildren(String goal){
        json=importJSON("https://en.wikipedia.org/w/api.php?action=query&format=json&prop=links&titles="+titleUsingUnderscores+"&formatversion=2&pllimit=max");
        if(json==null){
            childrenFound=true;
            return null;
        } else {
            String answerIsHere = getChildrenfromtheJSON(goal);
            if(answerIsHere!=null){
                return answerIsHere;
            }
            if (json.containsKey("batchcomplete")) {
                childrenFound = true;
            } else {
                JSONObject overArchingContinueObject = (JSONObject) json.get("continue");
                String restOfChildren = getRestOfChildren((String) overArchingContinueObject.get("plcontinue"), goal);
                if(restOfChildren!=null){
                    return restOfChildren;
                } else{
                    return null;
                }
            }
            return null;
        }
    }
    String getRestOfChildren(String plcontinue,String goal){

        this.json = importJSON(("https://en.wikipedia.org/w/api.php?action=query&format=json&prop=links&titles="+titleUsingUnderscores+"&formatversion=2&pllimit=max&plcontinue="+urlVersionOfPLContinue(plcontinue)));
        if(json!=null) {
            String answerIsHere = getChildrenfromtheJSON(goal);
            if(answerIsHere!=null){
                return answerIsHere;
            }
            if (json.containsKey("batchcomplete")) {
                childrenFound = true;
            } else {
                JSONObject overArchingContinueObject = (JSONObject) json.get("continue");
                String restOfChildren = getRestOfChildren((String) overArchingContinueObject.get("plcontinue"), goal);
                if(restOfChildren!=null){
                    return restOfChildren;
                } else{
                    return null;
                }
            }
        }
        return null;
    }

    String getChildrenfromtheJSON(String goal){
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
                        return("PATH FOUND: " + pathString + " -> " + title);
                    }
                }
            }
        }
        return null;
    }
    void findParents(){
        json = importJSON("https://en.wikipedia.org/w/api.php?action=query&format=json&prop=linkshere&titles="+titleUsingUnderscores+"&formatversion=2&lhprop=title&lhlimit=max");
        if(json==null){
            parentsFound = true;
        } else {
            getParentsfromtheJSON();
            if(json.containsKey("batchcomplete")){
                parentsFound = true;
            } else {
                JSONObject overArchingContinueObject = (JSONObject) json.get("continue");
                getRestOfParents((String)(overArchingContinueObject.get("lhcontinue")));
            }
        }

    }
    void getParentsfromtheJSON(){
        if(!(json==null)) {
            JSONObject query = (JSONObject) json.get("query");
            JSONArray pages = (JSONArray) query.get("pages");
            JSONObject content = (JSONObject) pages.getFirst();
            JSONArray linkshere = (JSONArray) content.get("linkshere");
            if(linkshere!=null) {
                for (int x = 0; x < linkshere.size(); x++) {
                    JSONObject objectInArray = (JSONObject) linkshere.get(x);
                    String title = (String) objectInArray.get("title");
                    pathString = title + " -> "+this.pathString;
                    parents.add(new Path(title, this.pathString));
                    System.out.println(title);
                }
            }
        }
    }
    void getRestOfParents(String lhcontinue){
        this.json = importJSON("https://en.wikipedia.org/w/api.php?action=query&format=json&prop=linkshere&titles="+titleUsingUnderscores+"&formatversion=2&lhprop=title&lhlimit=max&lhcontinue="+lhcontinue);
        if(json!=null){
            getParentsfromtheJSON();
            if(json.containsKey("batchcomplete")){
                parentsFound = true;
            } else {
                JSONObject overArchingContinueObject = (JSONObject) json.get("continue");
                getRestOfParents(((String) (overArchingContinueObject.get("lhcontinue"))));
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
