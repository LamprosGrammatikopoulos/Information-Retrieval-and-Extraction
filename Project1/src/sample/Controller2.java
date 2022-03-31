package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class Controller2 {

    @FXML
    private TextArea BodyText;

    @FXML
    private TextField PlacesText, PeopleText, TitleText;

    private String choosenFile="";

    public  void fillPlaces(String tmp, String choosenFile) {
        PlacesText.setText(tmp);
        this.choosenFile = choosenFile;
    }

    public  void fillPeople(String tmp, String choosenFile) {
        PeopleText.setText(tmp);
        this.choosenFile = choosenFile;
    }

    public  void fillTitle(String tmp, String choosenFile) {
        TitleText.setText(tmp);
        this.choosenFile = choosenFile;
    }

    public  void fillBody(String tmp, String choosenFile) {
        BodyText.setText(tmp);
        this.choosenFile = choosenFile;
    }

    @FXML
    public  void Update(ActionEvent event) {
        try {

            LuceneTester.deleteFile(choosenFile);
            System.out.println("Delete2===>"+choosenFile);

            File newFile = new File(choosenFile);
            BufferedWriter bw = new BufferedWriter(new FileWriter(newFile));
            bw.write("<PLACES>" + PlacesText.getText() + "</PLACES>\n");
            bw.write("<PEOPLE>" + PeopleText.getText() + "</PEOPLE>\n");
            bw.write("<TITLE>" + TitleText.getText() + "</TITLE>\n");
            bw.write("<BODY>" + BodyText.getText() + "\u0003</BODY>\n");
            bw.close();

            LuceneTester.addFile(choosenFile);

            Stage stage;
            stage = (Stage)BodyText.getScene().getWindow();
            stage.close();
        }
        catch(Exception e) {
            e.printStackTrace();
            System.out.println("An ERROR occurred while editing the file!");
        }
    }

    @FXML
    public  void Cancel(ActionEvent event) {
        Stage stage;
        stage = (Stage)BodyText.getScene().getWindow();
        stage.close();
    }
}
