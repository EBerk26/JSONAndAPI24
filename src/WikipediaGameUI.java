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
import static java.awt.Color.*;

public class WikipediaGameUI implements ActionListener {
    //the way I previously wrote the code doesn't lend itself so easily to a UI, so I'm making the UI over here, and then I can figure out how to add the pathfinding
    JFrame frame;
    JPanel backPanel;
    JPanel bottomPanel;
    JPanel upperBottomPanel;
    JTextArea output;
    JTextArea startArticleTextArea;
    JTextArea endArticleTextArea;
    JButton go;

    public static void main(String[] args) {
        new WikipediaGameUI();
    }
    public WikipediaGameUI(){
        frame = new JFrame("Wikipedia Pathfinder");
        frame.setSize(908,583);
        backPanel = new JPanel(new GridLayout(2,1));
        bottomPanel = new JPanel(new GridLayout(2,1));
        upperBottomPanel = new JPanel(new GridLayout(1,2));
        go = new JButton("GO");
        go.addActionListener(this);
        output = new JTextArea();
        output.setEditable(false);
        startArticleTextArea = new JTextArea("Start Article");
        endArticleTextArea = new JTextArea("Goal Article");
        frame.add(backPanel);
        backPanel.add(output);
        backPanel.add(bottomPanel);
        bottomPanel.add(upperBottomPanel);
        bottomPanel.add(go);
        upperBottomPanel.add(startArticleTextArea);
        upperBottomPanel.add(endArticleTextArea);
        go.setForeground(GREEN);
        go.setFont(new Font(null,Font.PLAIN,50));
        startArticleTextArea.setBackground(ORANGE);
        endArticleTextArea.setBackground(ORANGE);
        startArticleTextArea.setFont(new Font(null,Font.PLAIN,18));
        endArticleTextArea.setFont(new Font(null,Font.PLAIN,18));
        output.setFont(new Font(null,Font.PLAIN,25));
        output.setBackground(BLUE);
        output.setForeground(WHITE);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
    public void actionPerformed(ActionEvent e) { //this method came from ChatGPT as I could not figure out how to make the text appear before the pathfinding was finished -> changes to the UI inside the actionPerformed method happen all at once normally
        String startTitle = startArticleTextArea.getText();
        String endTitle = endArticleTextArea.getText();

        // Update UI immediately before starting pathfinding
        startArticleTextArea.setText(startTitle + "\n" + description(startTitle));
        endArticleTextArea.setText(endTitle + "\n" + description(endTitle));
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
                    //noinspection CallToPrintStackTrace
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
        for(int x =1;x<=170;x++){
            System.out.print("*");
        }
        System.out.println();
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
        while(noUnderscoresEditable.contains("&")){
            int indexOfAnd = noUnderscoresEditable.indexOf("&");
            noUnderscoresEditable=noUnderscoresEditable.substring(0,indexOfAnd)+"_"+noUnderscoresEditable.substring(indexOfAnd+1);
        }
        return noUnderscoresEditable;
    }


    JSONObject importJSON(String link){
        String output;
        StringBuilder jsonString= new StringBuilder();
        try {
            @SuppressWarnings("deprecation") URL url = new URL(link);
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
}