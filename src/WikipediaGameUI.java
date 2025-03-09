import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class WikipediaGameUI implements ActionListener {
    //the way I previously wrote the code doesn't lend itself so easily to a UI, so I'm making the UI over here and then I can figure out how to add the pathfinding
    JFrame frame;
    JPanel backPanel;
    JPanel bottomPanel;
    JPanel upperBottomPanel;
    TextArea output;
    TextArea startArticleTextArea;
    TextArea endArticleTextArea;
    JButton go;

    public static void main(String[] args) {
        new WikipediaGameUI();
    }
    public WikipediaGameUI(int test){
        System.out.println(description("Nökör"));
    }
    public WikipediaGameUI(){
        frame = new JFrame("Wikipedia Pathfinder");
        frame.setSize(908,583);
        backPanel = new JPanel(new GridLayout(2,1));
        bottomPanel = new JPanel(new GridLayout(2,1));
        upperBottomPanel = new JPanel(new GridLayout(1,2));
        go = new JButton("GO");
        go.addActionListener(this);
        output = new TextArea();
        output.setEditable(false);
        startArticleTextArea = new TextArea("Start Article");
        endArticleTextArea = new TextArea("Goal Article");
        frame.add(backPanel);
        backPanel.add(output);
        backPanel.add(bottomPanel);
        bottomPanel.add(upperBottomPanel);
        bottomPanel.add(go);
        upperBottomPanel.add(startArticleTextArea);
        upperBottomPanel.add(endArticleTextArea);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
    public void actionPerformed(ActionEvent e) {
        String startTitle = startArticleTextArea.getText();
        String endTitle = endArticleTextArea.getText();

        // Update UI immediately before starting pathfinding
        startArticleTextArea.setText(startTitle + " (" + description(startTitle) + ")");
        endArticleTextArea.setText(endTitle + " (" + description(endTitle) + ")");
        output.setText("Finding path...");

        // Run pathfinding in the background
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                return findPath(startTitle, endTitle);
            }

            @Override
            protected void done() {
                try {
                    output.setText(get()); // Update the UI with the found path
                } catch (Exception ex) {
                    output.setText("An error occurred.");
                    ex.printStackTrace();
                }
            }
        };
        worker.execute(); // Start the background task
    }

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
    String description(String title){
        JSONObject json = importJSON("https://en.wikipedia.org/w/api.php?action=query&format=json&prop=description&titles="+underscorify(title)+"&formatversion=2");
        if(json==null){
            return "Description Was Not Found";
        }
        JSONObject a = (JSONObject) json.get("query");
        JSONArray b = (JSONArray) a.get("pages");
        JSONObject c = (JSONObject) b.getFirst();
        if(c.containsKey("description")){
            return((String)c.get("description"));
        }
        return "Description Was Not Found";
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