package com.company;

import com.company.Graph;
import com.company.GraphNode;
import com.company.Website;
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

public class Main extends Application {
    static Graph graph;

    public static void main(String[] args) throws Exception {
        String filePath = System.getProperty("user.dir") + File.separator + "startURL.txt";
        BufferedReader Buff = new BufferedReader(new FileReader(filePath));

        String line = "";
        HashTable allWebsites = new HashTable();

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

      /*  System.out.println("Checking our storage");
        for (File dir : allDir) {
            if (dir.isFile() && dir.toString().endsWith(".txt") && !dir.toString().endsWith("startURL.txt")) {
                System.out.println("Adding from storage: " + dir.toString());
                Website w = Website.getPersistent(dir.toString());
                allWebsites.add(w);

                if(allWebsites.getSize() >= 500){
                    break;
                }
            }
        }

       */


        graph = new Graph();
        graph.buildGraph(500);
        // graph.drawGraph();

        graph.updateWithSimilarities();

        System.out.println("Graph edges:");
        graph.drawGraph(graph.edgeList);

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
        Text subTitle = new Text("Enter your source below:");
        subTitle.setFont(Font.font("Microsoft Uighur", FontWeight.NORMAL, FontPosture.REGULAR, 24));
        grid.getChildren().add(subTitle);
        GridPane.setConstraints(subTitle, 0, 5);

        //Making Another Title
        Text subTitle2 = new Text("Enter your sink below:");
        subTitle2.setFont(Font.font("Microsoft Uighur", FontWeight.NORMAL, FontPosture.REGULAR, 24));
        grid.getChildren().add(subTitle2);
        GridPane.setConstraints(subTitle2, 0, 12);

        //Making One More Title
        Text thirdTitle = new Text("Number of disjoint sets:");
        thirdTitle.setFont(Font.font("Microsoft Uighur", FontWeight.NORMAL, FontPosture.REGULAR, 24));
        grid.getChildren().add(thirdTitle);
        GridPane.setConstraints(thirdTitle, 0, 19);

        //Making One More Title
        Text fifthTitle = new Text("Shortest Path:");
        fifthTitle.setFont(Font.font("Microsoft Uighur", FontWeight.NORMAL, FontPosture.REGULAR, 24));
        grid.getChildren().add(fifthTitle);
        GridPane.setConstraints(fifthTitle, 0, 25);

        //Making a Text Field
        final TextField url = new TextField();
        url.setPromptText("Node");
        url.setPrefColumnCount(10);
        url.getText();
        GridPane.setConstraints(url,0,6);
        grid.getChildren().add(url);

        //Making a Text Field
        final TextField url2 = new TextField();
        url2.setPromptText("Node");
        url2.setPrefColumnCount(10);
        url2.getText();
        GridPane.setConstraints(url2,0,13);
        grid.getChildren().add(url2);

        //Making a Text Field (for output)
        TextField output = new TextField();
        grid.getChildren().add(output);
        GridPane.setConstraints(output, 0, 25);
        output.setPrefSize(5, 5);
        output.setEditable(false);

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

                String URL1 = url.getText();
                String URL2 = url2.getText();

                output.setText(Integer.toString(graph.countCycles()));

                GraphNode source = null;
                GraphNode sink = null;
                for(GraphNode gn: graph.nodeList){
                    if(gn.nodeData.getNodeName().equals(URL1)){
                        source = gn;
                    } else if(gn.nodeData.getNodeName().equals(URL2)){
                        sink = gn;
                    }
                }

                String outputtext4 = graph.drawGraph(graph.dijkstra(source, sink), source);
                System.out.println(outputtext4);
                output3.setText(outputtext4);

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
