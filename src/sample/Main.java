package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("search_engine.fxml"));
        primaryStage.setTitle("TReSA");
        primaryStage.setScene(new Scene(root, 1215, 800));
        primaryStage.show();
    }


    public static void main(String[] args) {
        LuceneTester.main(args);
        launch(args);
    }
}
