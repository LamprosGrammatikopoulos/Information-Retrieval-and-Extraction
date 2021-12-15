package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.lucene.queryparser.classic.ParseException;
import org.tartarus.snowball.ext.EnglishStemmer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class Controller{

    ObservableList observableList = FXCollections.observableArrayList();

    @FXML
    private ListView<String> ListView;

    @FXML
    private TextField SearchText;

    @FXML
    private AnchorPane AnchorPane;

    @FXML
    private CheckBox Places;
    @FXML
    private CheckBox People;
    @FXML
    private CheckBox Title;
    @FXML
    private CheckBox Body;
    @FXML
    private RadioButton StrictSearch;

    public static boolean varPlaces = true;
    public static boolean varPeople = true;
    public static boolean varTitle = true;
    public static boolean varBody = true;

    @FXML
    public void Check(ActionEvent event) {

        //Check selected tag
        if (Places.isSelected()) {
            varPlaces = true;
        }
        else {
            varPlaces = false;
        }
        if (People.isSelected()) {
            varPeople = true;
        }
        else {
            varPeople = false;
        }
        if (Title.isSelected()) {
            varTitle = true;
        }
        else {
            varTitle = false;
        }
        if (Body.isSelected()) {
            varBody = true;
        }
        else {
            varBody = false;
        }
    }

    @FXML
    public void Search(ActionEvent event) {

        LuceneTester tester;
        try {

            String query = "";

            //Problem when query has more than 2 words "billion national" ------> "billion n"
            //Stemmer receives word by word a phrase
            if((SearchText.getText().contains(" ") && SearchText.getLength()>1)) {
                query = SearchText.getText() + " ";
            }
            else {
                query = SearchText.getText();
            }

            if (query != "") {
                tester = new LuceneTester();
                EnglishStemmer stemmer = new EnglishStemmer();
                stemmer.setCurrent(query);
                stemmer.stem();
                String tmp = stemmer.getCurrent();

                System.out.println("stemQuery==>"+tmp);

                observableList = tester.search(tmp);
                ListView.setItems(observableList);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void Delete(ActionEvent event) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Txt files");
            fileChooser.setInitialDirectory(new File("res/Data"));
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Txt Files", "*.txt"));
            Stage stage = (Stage)AnchorPane.getScene().getWindow();
            List<File> selectedFiles = fileChooser.showOpenMultipleDialog(stage);
            if (selectedFiles != null) {
                for (File f : selectedFiles) {
                    System.out.println("Deleting: " + f.getPath());
                    LuceneTester.deleteFile(f.getPath());
                }
            }
            else {
                System.out.println("Txt file selection cancelled.");
            }
        }
        catch(Exception e) {
            System.out.println("An ERROR occurred while deleting the files!");
            return;
        }
    }

    @FXML
    public void Add(ActionEvent event) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Txt files");
            fileChooser.setInitialDirectory(new File("C:\\"));
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Txt Files", "*.txt"));
            Stage stage = (Stage)AnchorPane.getScene().getWindow();
            List<File> selectedFiles = fileChooser.showOpenMultipleDialog(stage);
            if (selectedFiles != null) {
                for (File f : selectedFiles) {
                    System.out.println("Adding: " + f.getPath());
                    LuceneTester.addFile(f.getPath());
                }
            }
            else {
                System.out.println("Txt file selection cancelled.");
            }
        }
        catch(Exception e) {
            System.out.println("An ERROR occurred while adding the files!");
            return;
        }
    }

    @FXML
    public void Edit(ActionEvent event) {
        try {

            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File("res/Data"));
            File selectedFile = fileChooser.showOpenDialog(null);
            System.out.println("Editing: " + selectedFile);

            if (selectedFile != null) {

                String choosenFile = selectedFile.getPath();

                String tmp = "";

                File file = new File(choosenFile);
                //index file contents
                BufferedReader br = new BufferedReader(new FileReader(file));
                String currentLine = "";
                while ((currentLine = br.readLine()) != null) {
                    tmp = tmp + currentLine + "\n";
                }
                br.close();

                FXMLLoader loader = new FXMLLoader(getClass().getResource("Edit-popup.fxml"));
                Parent root = loader.load();

                Controller2 scene2controller = loader.getController();

                scene2controller.showInfos(tmp,choosenFile);

                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.show();
            }
            else {
                System.out.println("Txt file selection cancelled.");
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            System.out.println("An ERROR occurred while editing the file!");
        }
    }

}



