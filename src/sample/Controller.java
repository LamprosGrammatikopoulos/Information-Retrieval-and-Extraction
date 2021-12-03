package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.NoSuchFileException;
import java.util.List;


public class Controller {
    @FXML
    private TextField SearchText;
    @FXML
    private TextArea TextPlace;
    @FXML
    private BorderPane BorderPane;

    private String choosenFile="";

    @FXML
    public void Delete(ActionEvent event) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Txt files");
            fileChooser.setInitialDirectory(new File("C:\\"));
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Txt Files", "*.txt"));
            Stage stage = (Stage)BorderPane.getScene().getWindow();
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
            Stage stage = (Stage)BorderPane.getScene().getWindow();
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
    public void Update(ActionEvent event) throws IOException {
        try {

            if (choosenFile != "null") {
                LuceneTester.deleteFile(choosenFile);

                File newFile = new File(choosenFile);
                BufferedWriter bw = new BufferedWriter(new FileWriter(newFile));
                bw.write(TextPlace.getText());

                LuceneTester.addFile(choosenFile);
                bw.close();
            }
            else {
                System.out.println("Txt file selection cancelled.");
            }
        }
        catch(Exception e) {
            System.out.println("An ERROR occurred while editing the file!");
            return;
        }





//        LuceneTester.deleteFile(SearchText.getText());
//        File newFile = new File(SearchText.getText());
//        BufferedWriter bw = new BufferedWriter(new FileWriter(newFile));
//        bw.write(TextPlace.getText());
//        bw.close();
//
//        LuceneTester.addFile(SearchText.getText());

        System.out.println("Updating: " + SearchText.getText());
    }

    @FXML
    public void Edit(ActionEvent event) {
        try {
            TextPlace.setText("");
            FileChooser fileChooser = new FileChooser();
            File selectedFile = fileChooser.showOpenDialog(null);
            System.out.println("Editing: " + selectedFile);
            if (selectedFile != null) {
                choosenFile=selectedFile.getPath();
                File file = new File(selectedFile.getPath());
                //index file contents
                BufferedReader br = new BufferedReader(new FileReader(file));
                String currentLine ="";
                while ((currentLine = br.readLine()) != null) {
                    TextPlace.setText(TextPlace.getText()+currentLine.toString()+"\n");
                }
                br.close();
            }
            else {
                System.out.println("Txt file selection cancelled.");
            }
        }
        catch(Exception e) {
            System.out.println("An ERROR occurred while editing the file!");
            return;
        }
    }
}
