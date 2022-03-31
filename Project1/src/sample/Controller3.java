package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;
import org.apache.lucene.queryparser.classic.ParseException;
import org.tartarus.snowball.ext.EnglishStemmer;

import java.io.IOException;

public class Controller3 {

    @FXML
    private TextArea Article;

    @FXML
    private TextField topK2;

    @FXML
    private ListView ListView2;

    public ObservableList observableList2 = FXCollections.observableArrayList();

    public static Boolean searchRelatedFlag = false;

    public String RelatedString ="";

    public void clickableFields() throws Exception {
        ListView2.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
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

    public  void fillArticle(String tmp, String relatedString) {
        Article.setText(Article.getText() + tmp);
        RelatedString = RelatedString + " " + relatedString;
    }



    @FXML
    public void SearchRelated(ActionEvent actionEvent) {
        ListView2.getItems().clear();
        if (topK2.getText().matches("^[0-9]+$")) {

            int intTopK = Integer.parseInt(topK2.getText());

            LuceneTester LuceneTester;
            try {
                //Search related articles via places,title,people

                //Query stemming
                LuceneTester = new LuceneTester();
                EnglishStemmer stemmer = new EnglishStemmer();
                stemmer.setCurrent(RelatedString);
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

                searchRelatedFlag = true;
                observableList2 = LuceneTester.search(tmp, intTopK);
                ListView2.getItems().addAll(observableList2);
                ListView2.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                clickableFields();
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
}
