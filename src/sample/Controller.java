package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.tartarus.snowball.ext.EnglishStemmer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class Controller {

    public ObservableList observableList = FXCollections.observableArrayList();

    @FXML
    public ListView<String> ListView;

    @FXML
    private TextField SearchText;

    @FXML
    private TextField TopK;

    @FXML
    private AnchorPane AnchorPane;

    @FXML
    private CheckBox Places, People, Title, Body;

    @FXML
    private Button closeHelpButton;

    public static boolean varPlaces = true, varPeople = true, varTitle = true, varBody = true;

    public static int intTopK = 0;


    public void clickableFields() throws Exception {
        ListView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                return new ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        if (item == null || empty) {
                            setText(null);
                            setStyle("-fx-control-inner-background: white ;");
                        }
                        else {
                            if (getIndex() % 4 == 0) {
                                setText(item);
                                setMouseTransparent(false);
                                setStyle("-fx-text-fill: blue; -fx-font: normal bold 20px 'serif'");
                            }
                            else if (getIndex() % 4 == 1) {
                                setText(item);
                                setStyle("-fx-text-fill: green;");
                                setMouseTransparent(true);
                            }
                            else {
                                setText(item);
                                setMouseTransparent(true);
                                setStyle("");
                            }

                        }
                    }
                };
            }
        });
    }

    @FXML
    public void openResultPopUp(javafx.scene.input.MouseEvent mouseEvent) {
        String selected = ListView.getSelectionModel().getSelectedItem();
        String choosenFile = selected;
        try {
            if (choosenFile != null) {

                DisplayArticles2("Show-article.fxml", choosenFile);
            }
            else {
                System.out.println("Please click on the article path.");
            }
        }
        catch(Exception e) {
            System.out.println("An error has occurred while opening the file!");
        }
    }

    @FXML
    public void CloseHelp(ActionEvent event) {
        Stage stage;
        stage = (Stage)closeHelpButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void Help(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Help-popup.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initOwner(Main.primaryStage);
            stage.show();
        }
        catch (IOException e){
            System.out.println("Error displaying help.");
        }
    }

    @FXML
    public void Check(ActionEvent event) {
        //Check selected tag
        if (Places.isSelected()) {
            varPlaces = true;
        } else {
            varPlaces = false;
        }
        if (People.isSelected()) {
            varPeople = true;
        } else {
            varPeople = false;
        }
        if (Title.isSelected()) {
            varTitle = true;
        } else {
            varTitle = false;
        }
        if (Body.isSelected()) {
            varBody = true;
        } else {
            varBody = false;
        }
    }

    @FXML
    public void Search(ActionEvent event) throws IOException {

        if (Places.isSelected() || People.isSelected() || Title.isSelected() || Body.isSelected()) {
            //Check if search is empty
            LuceneTester LuceneTester;
            try {
                String query = "";
                query = SearchText.getText();

                //Remove '?' from query
                query = query.replace("?", "");

                //Check if top-k is empty
                if (TopK.getText().matches("^[0-9]+$")) {
                    ListView.getItems().clear();

                    intTopK = Integer.parseInt(TopK.getText());

                    LuceneTester = new LuceneTester();

                    TokenStream tokenStream = new WhitespaceAnalyzer().tokenStream("contents", query);
                    //tokenStream = new LowerCaseFilter(tokenStream);
                    CharTermAttribute term = tokenStream.addAttribute(CharTermAttribute.class);
                    tokenStream.reset();

                    String tmp = "";
                    while (tokenStream.incrementToken()) {
                        //Stemmer receives word by word a line
                        EnglishStemmer stemmer = new EnglishStemmer();
                        stemmer.setCurrent(term.toString());
                        stemmer.stem();
                        tmp = tmp + stemmer.getCurrent() + " ";
                    }

                    //Date
                    if (tmp.contains("/")) {
                        tmp = tmp.replaceAll("/", ";");
                    }
                    //Time
                    if (tmp.contains(":")) {
                        tmp = tmp.replaceAll(":", "ω");
                    }

                    System.out.println("stemQuery==>"+tmp);

                    observableList = LuceneTester.search(tmp, intTopK);
                    ListView.getItems().addAll(observableList);
                    ListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                    clickableFields();
                }
                else {
                    Alert a = new Alert(Alert.AlertType.ERROR);
                    a.setTitle("Error");
                    a.setContentText("Please give an integer in \"Top-k results\" field.");
                    a.showAndWait();
                }
            }
            catch (Exception e) {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setTitle("Error");
                a.setContentText("Please give a supported query (find supported queries at \"Help\").");
                a.showAndWait();
            }
        }
        else {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Error");
            a.setContentText("Please select one of more fields to search.");
            a.showAndWait();
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
                try {
                    DisplayArticles1("Edit-popup.fxml", choosenFile);
                }
                catch(Exception e) {
                    System.out.println("An error has occurred while editing the file!");
                }
            }
            else {
                System.out.println("Txt file selection cancelled.");
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            System.out.println("An error has occurred while editing the file!");
        }
    }

    public void DisplayArticles1(String fmxl, String choosenFile) throws IOException {

        File file = new File(choosenFile);

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fmxl));
        Parent root = loader.load();
        Controller2 scene2controller = loader.getController();

        //index file contents
        BufferedReader br = new BufferedReader(new FileReader(file));
        String currentLine = "";
        String bodyText = "";

        int lineCounter = 0;
        while ((currentLine = br.readLine()) != null) {

            currentLine = currentLine.toString();

            //Tags Removal
            //Supposing that <PLACES></PLACES>, <PEOPLE></PEOPLE>, <TITLE></TITLE> are one line each
            //First line in txt (<PLACES></PLACES>)
            if(lineCounter == 0) {
                currentLine = currentLine.replaceAll("<PLACES>", "");
                currentLine = currentLine.replaceAll("</PLACES>", "");
                scene2controller.fillPlaces(currentLine,choosenFile);
            }
            //Second line in txt (<PEOPLE></PEOPLE>)
            else if(lineCounter == 1) {
                currentLine = currentLine.replaceAll("<PEOPLE>", "");
                currentLine = currentLine.replaceAll("</PEOPLE>", "");
                scene2controller.fillPeople(currentLine,choosenFile);
            }
            //Third line in txt (<TITLE></TITLE>)
            else if(lineCounter == 2) {
                currentLine = currentLine.replaceAll("<TITLE>", "");
                currentLine = currentLine.replaceAll("</TITLE>", "");
                scene2controller.fillTitle(currentLine,choosenFile);
            }
            //Fourth line in txt (<BODY></BODY>)
            else if(lineCounter == 3 && currentLine.contains("</BODY>")) {
                currentLine = currentLine.replaceAll("<BODY>", "");
                currentLine = currentLine.replaceAll("</BODY>", "");
                bodyText=bodyText+currentLine+"\n";
                scene2controller.fillBody(bodyText,choosenFile);
                break;
            }
            //Last line in txt (<BODY>)
            else if (lineCounter >= 3 && !currentLine.contains("</BODY>")) {
                if (currentLine.contains("<BODY>")) {
                    currentLine = currentLine.replaceAll("<BODY>", "");
                }
                bodyText=bodyText+currentLine+"\n";
            }
            //Last line in txt (</BODY>)
            else if (currentLine.contains("\u0003</BODY>")) {
                scene2controller.fillBody(bodyText,choosenFile);
                break;
            }

            lineCounter++;
        }
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.initOwner(Main.primaryStage);
        stage.show();
    }

    public void DisplayArticles2(String fmxl, String choosenFile) throws IOException {

        File file = new File(choosenFile);

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fmxl));
        Parent root = loader.load();
        Controller3 scene3controller = loader.getController();

        //index file contents
        BufferedReader br = new BufferedReader(new FileReader(file));
        String currentLine = "";
        String bodyText = "";

        int lineCounter = 0;
        while ((currentLine = br.readLine()) != null) {

            currentLine = currentLine.toString();

            //Tags Removal
            //Supposing that <PLACES></PLACES>, <PEOPLE></PEOPLE>, <TITLE></TITLE> are one line each
            //First line in txt (<PLACES></PLACES>)
            if(lineCounter == 0 && !currentLine.equals("")) {
                currentLine = currentLine.replaceAll("<PLACES>", "");
                currentLine = currentLine.replaceAll("</PLACES>", "");
                scene3controller.fillArticle(currentLine+"\n"+"\n",currentLine);
            }
            //Second line in txt (<PEOPLE></PEOPLE>)
            else if(lineCounter == 1 && !currentLine.equals("")) {
                currentLine = currentLine.replaceAll("<PEOPLE>", "");
                currentLine = currentLine.replaceAll("</PEOPLE>", "");
                scene3controller.fillArticle(currentLine+"\n"+"\n",currentLine);
            }
            //Third line in txt (<TITLE></TITLE>)
            else if(lineCounter == 2 && !currentLine.equals("")) {
                currentLine = currentLine.replaceAll("<TITLE>", "");
                currentLine = currentLine.replaceAll("</TITLE>", "");
                scene3controller.fillArticle(currentLine+"\n"+"\n",currentLine);
            }
            //Fourth line in txt (<BODY></BODY>)
            else if(lineCounter == 3 && currentLine.contains("</BODY>")) {
                currentLine = currentLine.replaceAll("<BODY>", "");
                currentLine = currentLine.replaceAll("</BODY>", "");
                bodyText=bodyText+currentLine+"\n";
                scene3controller.fillArticle(bodyText,"");
                break;
            }
            //Last line in txt (<BODY>)
            else if (lineCounter >= 3 && !currentLine.contains("</BODY>")) {
                if (currentLine.contains("<BODY>")) {
                    currentLine = currentLine.replaceAll("<BODY>", "");
                }
                bodyText=bodyText+currentLine+"\n";
            }
            //Last line in txt (</BODY>)
            else if (currentLine.contains("\u0003</BODY>")) {
                scene3controller.fillArticle(bodyText,"");
                break;
            }
            lineCounter++;
        }
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.initOwner(Main.primaryStage);
        stage.show();
    }
}



