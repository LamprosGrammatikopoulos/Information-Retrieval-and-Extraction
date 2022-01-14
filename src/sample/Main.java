package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class Main extends Application {

    public static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception{
        this.primaryStage = primaryStage;
        Parent root = FXMLLoader.load(getClass().getResource("search_engine.fxml"));
        primaryStage.setTitle("TReSA");
        primaryStage.setScene(new Scene(root, 1215, 800));
        primaryStage.show();
    }

    public static void main(String[] args) {

        File file = new File("res\\Index");

        if (file.isDirectory()) {
            if (file.list().length<=0) {
                //Creating the index when the program starts
                LuceneTester tester;
                try {
                    tester = new LuceneTester();
                    tester.createIndex();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        launch(args);
    }
}
