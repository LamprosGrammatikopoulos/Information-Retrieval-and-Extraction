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
import org.apache.lucene.queryparser.classic.ParseException;
import org.tartarus.snowball.ext.EnglishStemmer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class Controller {

    ObservableList observableList = FXCollections.observableArrayList();

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
                DisplayArticles("Show-article.fxml", choosenFile);
            }
            else {
                System.out.println("Please click on the article path.");
            }
        }
        catch(Exception e) {
            System.out.println("An error has occurred while editing the file!");
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
            stage.show();
        }
        catch (IOException e){
            System.out.println("Error displaying help.");
        }
    }

    @FXML
    public void Check(ActionEvent event) {

        if (Places.isSelected() || People.isSelected() || Title.isSelected() || Body.isSelected()) {
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
        else {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Error");
            a.setContentText("Please select one of more fields to search.");
            a.showAndWait();
        }

    }

    @FXML
    public void Search(ActionEvent event) {
        ListView.getItems().clear();
        if (TopK.getText().matches("^[0-9]+$")) {

            intTopK = Integer.parseInt(TopK.getText());

            LuceneTester LuceneTester;
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

                if (SearchText.getText().matches("([\"]?[0-9a-zA-Z ]+[-.,:\\/][0-9a-zA-Z]+[-.,:\\/][0-9a-zA-Z ]+[\"]?)|([\"]?[0-9a-zA-Z ]+[-.,:\\/][0-9a-zA-Z ]+[\"]?)|([\"]?[0-9a-zA-Z ]+[\"]?)")) {
                    LuceneTester = new LuceneTester();
                    EnglishStemmer stemmer = new EnglishStemmer();
                    stemmer.setCurrent(query);
                    stemmer.stem();
                    String tmp = stemmer.getCurrent();

                    //Date
                    if (tmp.contains("/")) {
                        tmp = tmp.replaceAll("/", ";");
                    }
                    //Time
                    if (tmp.contains(":")) {
                        tmp = tmp.replaceAll(":", ";");
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
                    a.setContentText("Please give a supported query (find supported queries at \"Help\").");
                    a.showAndWait();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            catch (ParseException e) {
                e.printStackTrace();
            }
            catch (Exception e) {
                e.printStackTrace();
            }

        }
        else {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Error");
            a.setContentText("Please give an integer in \"Top-k results\" field.");
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
                    DisplayArticles("Edit-popup.fxml", choosenFile);
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

    public void DisplayArticles(String fmxl, String choosenFile) throws IOException {
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
                currentLine = currentLine.substring(8,currentLine.length()-9);
                scene2controller.fillPlaces(currentLine,choosenFile);
            }
            //Second line in txt (<PEOPLE></PEOPLE>)
            else if(lineCounter == 1) {
                currentLine = currentLine.substring(8,currentLine.length()-9);
                scene2controller.fillPeople(currentLine,choosenFile);
            }
            //Third line in txt (<TITLE></TITLE>)
            else if(lineCounter == 2) {
                currentLine = currentLine.substring(7,currentLine.length()-8);
                scene2controller.fillTitle(currentLine,choosenFile);
            }
            //Fourth line in txt (<BODY></BODY>)
            else if(lineCounter == 3 && currentLine.contains("</BODY>")) {
                currentLine = currentLine.substring(6,currentLine.length()-7);
                bodyText=bodyText+currentLine+"\n";
                scene2controller.fillBody(bodyText,choosenFile);
                break;
            }
            //Last line in txt (<BODY>)
            else if (lineCounter >= 3 && !currentLine.contains("</BODY>")) {
                if (currentLine.contains("<BODY>")) {
                    currentLine = currentLine.substring(6);
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
        stage.show();
    }
}



