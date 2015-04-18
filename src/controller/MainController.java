package controller;

import com.sun.deploy.ui.ProgressDialog;
import database.Database;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.PopupWindow;
import javafx.stage.Stage;
import main.Main;

import javax.print.DocFlavor;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class MainController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    @FXML
    private void connection(ActionEvent event) {
        Scene scene = (Scene) ((Button) event.getSource()).getScene();
        Stage stage = (Stage) scene.getWindow();

        try {
            Database.connect(usernameField.getText(), passwordField.getText());
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText("Error while connecting to the database");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
            return;
        }

        stage.setScene(new Scene(new Pane()));
        /*
        try {
            String query = "SELECT * FROM COMPANY WHERE ROWNUM <= 10";
            PreparedStatement preparedStatement = con.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()) {
                System.out.println(resultSet.getString(2));
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }*/
    }
}
