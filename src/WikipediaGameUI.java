import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
        output.setText("Finding path...");
        String path = findPath(startArticleTextArea.getText(),endArticleTextArea.getText());
        output.setText(path);
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
        goalPages.addFirst(new Path(goalTitle,""));
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
            }
        }
        return "failure";
    }

}