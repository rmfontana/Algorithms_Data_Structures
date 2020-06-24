import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Assignment1 extends Application {

    static int maxSites = 20;
    static ArrayList<Website> startSites = new ArrayList<>();
    static HashTable table = new HashTable();

    public static void main(String[] args) throws Exception {
        String filePath = System.getProperty("user.dir") + File.separator + "startURL.txt";
        BufferedReader Buff = new BufferedReader(new FileReader(filePath));

        String line = "";

        // Put all starting in hashmap
        while ((line = Buff.readLine()) != null) {
            if (Website.checkPersistent(line)) {
                // if we already have some info
                Website w = Website.getPersistent(line);
                startSites.add(w);
                table.add(w);
            } else {
                // if website is not persistent
                Website w = new Website(line);
                w.getContents();
                w.getConnectedSites();
                w.setWords();
                w.makePersistent();
                startSites.add(w);
                table.add(w);
            }
        }


        int addedFromStorage = 0;
        int webCrawl = 0;

        // get the rest of the 20 links
        if (table.getSize() >= maxSites) {
            System.exit(0);
        }

        // first see if we have any persistent links lying around
        File checkDir = new File(System.getProperty("user.dir"));
        File[] allDir = checkDir.listFiles();

        System.out.println("Checking our storage");
        for (File dir : allDir) {
            if (dir.isFile() && dir.toString().endsWith(".txt") && !dir.toString().endsWith("startURL.txt")) {
                Website w = Website.getPersistent(dir.toString());
                addedFromStorage += 1;
                table.add(w);
            }
        }

        // if we dont have enough persistent get some more
        if (table.getSize() < maxSites) {
            for (Website w : startSites) {
                System.out.println(w.URL);
                System.out.println(w.connectedSites);
                for (String URL : w.connectedSites) {
                    Website anotherSite = new Website(URL);
                    anotherSite.getContents();
                    anotherSite.getConnectedSites();
                    anotherSite.setWords();

                    if(anotherSite.words.values().size() > 1000){
                        anotherSite.makePersistent();
                        table.add(anotherSite);
                        webCrawl += 1;
                    }


                    if (table.getSize() >= maxSites) {
                        System.out.println("Added " + addedFromStorage + " sites from storage");
                        System.out.println("Added " + webCrawl + " sites from web crawling");
                        break;
                    }
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
        thirdTitle.setFont(Font.font("Microsoft Uighur", FontWeight.NORMAL, FontPosture.REGULAR, 30));
        grid.getChildren().add(thirdTitle);
        GridPane.setConstraints(thirdTitle, 0, 14);

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
                    Website yourWebsite = new Website(submitURL);
                    yourWebsite.getContents();
                    yourWebsite.getConnectedSites();
                    yourWebsite.setWords();
                    String outputText = Website.similarity(yourWebsite, table.collectAll()).URL;
                    output.setText(outputText);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //Putting it all together
        primaryStage.setTitle("Website Find-o-matic");
        //StackPane root = new StackPane();
        Scene scene = new Scene(grid, 700, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
        submit.setDefaultButton(true);
        submit.setCursor(Cursor.HAND);
    }
}
