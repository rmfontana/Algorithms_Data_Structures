import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class main extends Application {
    static ArrayList<BTree> allbtrees;


    public static ArrayList<BTree> getAllPersistent(Hashtable websites) throws IOException {
        ArrayList<BTree> allBTree = new ArrayList<BTree>();

        for(Website website: websites.collectAll()){
            String URL = website.getCutURL(website.URL);
            System.out.println("Getting from storage: " + URL);

            BTree bt = new BTree(URL);
            bt.root = bt.diskRead(0);
            bt.writeToCache(bt.root);

            // for debugging
            System.out.println("Rootpos: " + bt.root.position);
            System.out.println("Rootpar: " + bt.root.parent);
            System.out.println("Root keys: ");
            for(Map.Entry<Integer, Integer> entry : bt.root.keys.entrySet()){
                System.out.println(entry.getKey());
            }

            System.out.println("Root children: ");
            for(int i=0; i< bt.root.children.length; i++){
                System.out.println(bt.root.children[i]);
            }

            bt.getAllChildren(bt.root);

            for(BTreeNode btn: bt.collectAllChildren){
                bt.writeToCache(btn);
            }

            allBTree.add(bt);
        }

        return allBTree;
    }


    public static void main(String[] args) throws Exception {
        String filePath = System.getProperty("user.dir") + File.separator + "startURL.txt";
        BufferedReader Buff = new BufferedReader(new FileReader(filePath));

        String line = "";
        allbtrees = new ArrayList<BTree>();
        Hashtable allWebsites = new Hashtable();

        while ((line = Buff.readLine()) != null) {
            Website w = new Website(line);
            w.getContents();
            w.getConnectedSites();
            w.setWords();
            w.makePersistent();
            allWebsites.add(w);
        }

        File checkDir = new File(System.getProperty("user.dir"));
        File[] allDir = checkDir.listFiles();

        System.out.println("Checking our storage");
        for (File dir : allDir) {
            if (dir.isFile() && dir.toString().endsWith(".txt") && !dir.toString().endsWith("startURL.txt")) {
                System.out.println("Adding from storage: " + dir.toString());
                Website w = Website.getPersistent(dir.toString());
                allWebsites.add(w);

                if(allWebsites.getSize() >= 100){
                    break;
                }
            }
        }

        while(allWebsites.getSize() < 100) {
            for (Website w : allWebsites.collectAll()) {
                System.out.println("Collecting new websites");
                for (int i = 0; i < 20; i++) {
                    System.out.println("Web crawling");
                    String s = w.connectedSites.get(i);
                    Website website = new Website(s);
                    website.getContents();
                    website.getConnectedSites();
                    website.setWords();
                    website.makePersistent();
                    allWebsites.add(website);

                    if(allWebsites.getSize() >= 100){
                        break;
                    }

                }
            }

        }

        File file = new File("C:/Users/Rose/IdeaProjects/project2-csc365-takemaybe5/BTree/");

        if(file.list().length != 0) {
            System.out.println("Getting btrees from storage: ");
            allbtrees = getAllPersistent(allWebsites);
        }

        System.out.println("all betree size" + allbtrees.size());
        if(allbtrees.size() < 100) {

            for (Website w : allWebsites.collectAll()) {

                String URL = w.getCutURL(w.URL);
                BTree bt = new BTree(URL);
                Map<String, Integer> words = w.words;
                for (Map.Entry<String, Integer> entry : words.entrySet()) {
                    int newentry = entry.getKey().hashCode();
                    int newvalue = entry.getValue();

                    // for debugging
                    System.out.println("Inserted into BTree: " + newentry + " " + newvalue);
                    bt.BTreeInsert(newentry, newvalue);
                }

                // disk writing ALL from cache #speed that  up
                ArrayList<BTreeNode> allnodes = new ArrayList<>();
                allnodes.addAll(bt.cache.values());
                bt.diskWrite(allnodes);

                // for debugging
                System.out.println("Final Cache:");
                for(Map.Entry<Long, BTreeNode> entry: bt.cache.entrySet()) {
                    System.out.println("Node Start");
                    long key = entry.getKey();
                    BTreeNode value = entry.getValue();
                    System.out.println("Position: " + key);
                    System.out.println("Parent: " + value.parent);
                    System.out.println("KeysValues: ");

                    for (Object o : value.keysToArray()) {
                        System.out.println(o.toString());
                    }

                    System.out.println("Leaf status: " + value.leaf);
                    System.out.println("Children: ");

                    for (long child : value.children) {
                        if (child != -1) {
                            System.out.println(child);
                        }
                    }
                }


                allbtrees.add(bt);

                if(allbtrees.size() >= 100){
                    break;
                }
            }
        }
        
    launch(args);
}

    @Override
    public void start(Stage primaryStage) {
        //Creating a GridPane container
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(5);
        grid.setHgap(5);

        //Making the Title
        Text mainTitle = new Text("Welcome to the Website Database!");
        mainTitle.setFont(Font.font("Microsoft Uighur", FontWeight.NORMAL, FontPosture.REGULAR, 36));
        grid.getChildren().add(mainTitle);
        GridPane.setConstraints(mainTitle, 0,0);

        //Making Another Title
        Text subTitle = new Text("Enter your URL to compare below:");
        subTitle.setFont(Font.font("Microsoft Uighur", FontWeight.NORMAL, FontPosture.REGULAR, 30));
        grid.getChildren().add(subTitle);
        GridPane.setConstraints(subTitle, 0, 5);

        //Making One More Title
        Text thirdTitle = new Text("Best match found:");
        thirdTitle.setFont(Font.font("Microsoft Uighur", FontWeight.NORMAL, FontPosture.REGULAR, 24));
        grid.getChildren().add(thirdTitle);
        GridPane.setConstraints(thirdTitle, 0, 14);

        //Making One More Title
        Text fourthTitle = new Text("Best cluster found:");
        fourthTitle.setFont(Font.font("Microsoft Uighur", FontWeight.NORMAL, FontPosture.REGULAR, 24));
        grid.getChildren().add(fourthTitle);
        GridPane.setConstraints(fourthTitle, 0, 20);

        //Making One More Title
        Text fifthTitle = new Text("Other pages in cluster:");
        fifthTitle.setFont(Font.font("Microsoft Uighur", FontWeight.NORMAL, FontPosture.REGULAR, 24));
        grid.getChildren().add(fifthTitle);
        GridPane.setConstraints(fifthTitle, 0, 26);

        //Making a Text Field
        final TextField url = new TextField();
        url.setPromptText("URL");
        url.setPrefColumnCount(10);
        url.getText();
        GridPane.setConstraints(url,0,6);
        grid.getChildren().add(url);

        //Making a Text Field (for output)
        TextField output = new TextField();
        grid.getChildren().add(output);
        GridPane.setConstraints(output, 0, 15);
        output.setPrefSize(5, 5);
        output.setEditable(false);

        //Making a Text Field (for output)
        TextField output2 = new TextField();
        grid.getChildren().add(output2);
        GridPane.setConstraints(output2, 0, 22);
        output2.setPrefSize(5, 5);
        output2.setEditable(false);

        // making textarea for output
        TextArea output3 = new TextArea();
        grid.getChildren().add(output3);
        GridPane.setConstraints(output3, 0, 28);
        output3.setWrapText(true);

        //Defining the Submit button
        Button submit = new Button("Submit");
        GridPane.setConstraints(submit,1,6);
        grid.getChildren().add(submit);


        //Making the Submit button do stuff
        submit.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                // Create comparison table from given url
                String submitURL = url.getText();

                if(!submitURL.startsWith("https://") && !submitURL.startsWith("http://")){
                    String URLprefix = "https://en.wikipedia.org/wiki/";
                    submitURL = URLprefix + submitURL;
                }

                try {
                    Website entryWebsite = new Website(submitURL);
                    entryWebsite.getConnectedSites();
                    entryWebsite.getContents();
                    entryWebsite.setWords();

                    BTree entryBT = new BTree(entryWebsite.getCutURL(entryWebsite.URL));
                    Map<String, Integer> words = entryWebsite.words;
                    for(Map.Entry<String, Integer> entry: words.entrySet()){
                        int newentry = entry.getKey().hashCode();
                        int newvalue = entry.getValue();
                        entryBT.BTreeInsert(newentry, newvalue);
                    }



                    similarity sessionmetrics = new similarity();

                    sessionmetrics.kmeans(entryBT, allbtrees, 10);
                    HashMap<Double, BTree> bestCluster = sessionmetrics.getBestCluster();
                    System.out.println("Best Cluster: " + sessionmetrics.getCluster());

                    String outputText3 = "";
                    System.out.println("Cluster entries: ");
                    for(Map.Entry<Double, BTree> entry: bestCluster.entrySet()){
                        System.out.println(entry.getValue().URL);
                        outputText3 = entry.getValue().URL + "\n" + outputText3;
                    }

                    System.out.println("Best match: ");
                    System.out.println(sessionmetrics.getBestURL());

                    String outputText = sessionmetrics.getBestURL();
                    String outputText2 = Integer.toString(sessionmetrics.getCluster());

                    output.setText(outputText);
                    output2.setText(outputText2);
                    output3.setText(outputText3);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //Putting it all together
        primaryStage.setTitle("Website Find-o-matic");
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        Scene scene = new Scene(grid, screenBounds.getWidth(), screenBounds.getHeight());
        primaryStage.setScene(scene);
        primaryStage.show();
        submit.setDefaultButton(true);
        submit.setCursor(Cursor.HAND);
    }
}
