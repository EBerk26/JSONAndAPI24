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
import java.util.Arrays;

public class WikipediaGameV2 {
    //TO DO: Make it so that we don't find the children from the same page multiple times, do some back tracking from the end, heuristic function
    public String findPath(String startTitle, String goalTitle) {
        WikipediaPage currentPage;
        ArrayList<Path> queue = new ArrayList<>();
        ArrayList<Path> goalPages = new ArrayList<>();
        ArrayList<String> pagesWithFoundChildren = new ArrayList<>();
        WikipediaPage endPage = new WikipediaPage(goalTitle,"",true,null);
        endPage.findParents();
        for(Path p: endPage.parents){
            goalPages.addLast(p);
        }
        goalPages.addFirst(new Path(goalTitle,goalTitle));
        WikipediaPage startPage = new WikipediaPage(startTitle,"",false,goalPages);
        if(startPage.findChildren()!=null){
            return startPage.findChildren();
        }
        pagesWithFoundChildren.add(startPage.title);

        for(Path p: startPage.children){
          queue.addLast(p);
        }
        while(!queue.isEmpty()) {
            if(!pagesWithFoundChildren.contains(queue.getFirst().title)) {
                currentPage = new WikipediaPage(queue.getFirst().title, queue.getFirst().path, false,goalPages);
                if(currentPage.findChildren()!=null){
                    return currentPage.findChildren();
                }
                pagesWithFoundChildren.add(currentPage.title);
                for (Path p : currentPage.children) {
                    if (p.title.contains(goalTitle) || p.title.contains(goalTitle.toLowerCase())) { //if the previous title is contained somewhere it is prioritized.
                        queue.addFirst(p);
                    } else {
                        queue.addLast(p);
                    }
                }
                queue.removeFirst();
            } else{
                queue.removeFirst();
            }
        }
        return "failure";
    }
    public WikipediaGameV2(){
        System.out.println(findPath("Luzon water redstart","Dwayne Johnson"));
    }
    public static void main(String[] args) {
        new WikipediaGameV2();
    }
}

class WikipediaPage {
    String title;
    char[] accents;
    String titleUsingUnderscores;
    JSONObject json;
    ArrayList<Path> children = new ArrayList<>();
    boolean childrenFound = false;
    boolean parentsFound = true;
    ArrayList<Path> parents = new ArrayList<>();
    String pathString = "";
    ArrayList<Path> goalPages;
    public WikipediaPage(String title,String previousPath,boolean backTracking,ArrayList<Path> goalPages) {
        ArrayList<Character> ArrayListaccents = new ArrayList<>(Arrays.asList('à','À','á','Á','â','Â','ã','Ã','ä','Ä','å','Å','æ','Æ','œ','Œ','ç','Ç','ð','Ð','ø','Ø','ß','è','È','é','É','ê','Ê','ñ','Ñ','ë','Ë','ì','Ì','í','Í','î','Î','õ','Õ','ï','Ï','ò','Ò','ó','Ó','ô','Ô','ö','Ö','ù','Ù','ú','Ú','û','Û','ü','Ü','ý','Ý','ÿ','Ÿ'));
        Object[] objectAccents = ArrayListaccents.toArray();
        accents = new char[objectAccents.length];
        for(int x =0;x<objectAccents.length;x++){
            accents[x] = (Character)(objectAccents[x]);
        }
        this.title = title;
        this.titleUsingUnderscores = underscorify(title);
        if(goalPages!=null) {
            this.goalPages = new ArrayList<>(goalPages);
        }
        if(backTracking){
            if(previousPath.isEmpty()){
                pathString = this.title;
            } else {
                pathString = this.title + "\n" + pathString;
            }
        } else if(previousPath.isEmpty()){
            pathString = title;
        } else {
            pathString += (previousPath + "\n" + title);
        }


    }
    String findChildren(){
        json=importJSON(chopOffAccents("https://en.wikipedia.org/w/api.php?action=query&format=json&prop=links&titles="+titleUsingUnderscores+"&formatversion=2&plnamespace=0&pllimit=max"));
        if(json==null){
            childrenFound=true;
        } else {
            String answerIsHere = getChildrenfromtheJSON();
            if(answerIsHere!=null){
                return answerIsHere;
            }
            if (json.containsKey("batchcomplete")) {
                childrenFound = true;
            } else {
                JSONObject overArchingContinueObject = (JSONObject) json.get("continue");
                return getRestOfChildren((String) overArchingContinueObject.get("plcontinue"));
            }
        }
        return null;
    }
    String getRestOfChildren(String plcontinue){
        this.json = importJSON(chopOffAccents(("https://en.wikipedia.org/w/api.php?action=query&format=json&prop=links&titles="+titleUsingUnderscores+"&formatversion=2&plnamespace=0&pllimit=max&plcontinue="+urlVersionOfPLContinue(plcontinue))));
        if(json!=null) {
            String answerIsHere = getChildrenfromtheJSON();
            if(answerIsHere!=null){
                return answerIsHere;
            }
            if (json.containsKey("batchcomplete")) {
                childrenFound = true;
            } else {
                JSONObject overArchingContinueObject = (JSONObject) json.get("continue");
                return getRestOfChildren((String) overArchingContinueObject.get("plcontinue"));
            }
        }
        return null;
    }

    String getChildrenfromtheJSON(){
        if(!(json==null)) {
            JSONObject query = (JSONObject) json.get("query");
            JSONArray pages = (JSONArray) query.get("pages");
            JSONObject content = (JSONObject) pages.getFirst();
            JSONArray links = (JSONArray) content.get("links");
            if(links!=null) {
                for (Object link : links) {
                    JSONObject objectInArray = (JSONObject) link;
                    String title = (String) objectInArray.get("title");
                    children.add(new Path(title, this.pathString));
                    System.out.println(title);
                    if (titleIsAGoalPage(title, goalPages)) {
                        int indexOfFoundPath = indexOfTitleInGoalPages(title);
                        return (pathString + "\n" + goalPages.get(indexOfFoundPath).path);
                    }
                }
            }
        }
        return null;
    }
    int indexOfTitleInGoalPages(String title){
        for(int x=0;x<goalPages.size();x++){
            if(title.equals(goalPages.get(x).firstTitle)||title.equals(goalPages.get(x).title)){
                return x;
            }
        }
        return -1;
    }

    boolean titleIsAGoalPage(String title,ArrayList<Path> goalPages){
        if(goalPages!=null) {
            for (Path p : goalPages) {
                if (title.equals(p.firstTitle)||title.equals(p.title)) {
                    return true;
                }
            }
        }
        return false;
    }
    void findParents(){
        json = importJSON(chopOffAccents("https://en.wikipedia.org/w/api.php?action=query&format=json&prop=linkshere&titles="+titleUsingUnderscores+"&formatversion=2&lhnamespace=0&lhprop=title&lhlimit=max"));
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
                for (Object o : linkshere) {
                    JSONObject objectInArray = (JSONObject) o;
                    String title = (String) objectInArray.get("title");
                    String pathString = title + "\n" + this.pathString;
                    parents.add(new Path(this.title, pathString, title));
                    System.out.println(title);
                }
            }
        }
    }
    void getRestOfParents(String lhcontinue){
        this.json = importJSON(chopOffAccents("https://en.wikipedia.org/w/api.php?action=query&format=json&prop=linkshere&titles="+titleUsingUnderscores+"&formatversion=2&lhnamespace=0&lhprop=title&lhlimit=max&lhcontinue="+lhcontinue));
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
            @SuppressWarnings("deprecation") URL url = new URL(link); // Your API's URL goes here
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
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            return null;
        }
    }
    String chopOffAccents(String link){
        char chartAtX;
        if(link.contains("continue=")) {
            for (int x = link.indexOf("continue="); x < link.length(); x++) {
                chartAtX = link.charAt(x);
                if (isAccented(chartAtX)) {
                    return link.substring(0, x);
                }
            }
        }
        return link;
    }
    boolean isAccented(Character input){
        for (char accent : accents) {
            if (accent == input) {
                return true;
            }
        }
        return false;
    }
}
class Path{
    String title;
    String path; //this goes until the one right before the title.
    String firstTitle;
    public Path(String title, String path) {
        this.title = title;
        this.path = path;
    }
    public Path(String title, String path,String firstTitle) {
        this.title = title;
        this.path = path;
        this.firstTitle = firstTitle;
    }
}