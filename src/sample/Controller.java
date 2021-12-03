package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class Controller {
    @FXML
    private TextField SearchText;

    @FXML
    void Search(ActionEvent event) {
        System.out.println(SearchText.getText());
    }
}