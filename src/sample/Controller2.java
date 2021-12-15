package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class Controller2 {

    @FXML
    private TextArea TextArea;

    private String choosenFile="";

    public  void showInfos(String tmp,String choosenFile) {
        TextArea.setText(tmp);
        this.choosenFile = choosenFile;
    }

    @FXML
    public  void Update(ActionEvent event) {
        try {

            LuceneTester.deleteFile(choosenFile);

            File newFile = new File(choosenFile);
            BufferedWriter bw = new BufferedWriter(new FileWriter(newFile));
            bw.write(TextArea.getText());

            LuceneTester.addFile(choosenFile);
            bw.close();

            Stage stage;
            stage=(Stage)TextArea.getScene().getWindow();
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
        stage = (Stage)TextArea.getScene().getWindow();
        stage.close();
    }
}
