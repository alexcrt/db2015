package controller;

import database.Database;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;

public class ConnectionController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;

    @FXML
    private void connection(ActionEvent event) {
        try {
            Iterator<String> ite = Files.readAllLines(Paths.get("login")).iterator();
            usernameField.setText(ite.next());
            passwordField.setText(ite.next());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scene scene = (Scene) ((Button) event.getSource()).getScene();
        Stage stage = (Stage) scene.getWindow();

        Task<Void> connectingTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Database.connect(usernameField.getText(), passwordField.getText());
                return null;
            }
        };

        startProgressTask(stage, connectingTask);
    }

    private void startProgressTask(Stage owner, Task<Void> connectingTask) {

        try {
            Parent root = FXMLLoader.load(getClass().getResource("../resources/progress_task.fxml"));

            Stage progressStage = new Stage();
            Scene progressScene = new Scene(root);

            ((ProgressBar) progressScene.lookup("#progressBarTask")).setProgress(-1);

            progressStage.setScene(progressScene);
            progressStage.setTitle("Connecting to database");
            progressStage.initModality(Modality.APPLICATION_MODAL);
            progressStage.initOwner(owner);
            progressStage.setOnCloseRequest(e -> connectingTask.cancel());

            connectingTask.setOnSucceeded(e -> {
                progressStage.close();
                try {
                    Parent newRoot = FXMLLoader.load(getClass().getResource("../resources/main_panel.fxml"));
                    owner.setScene(new Scene(newRoot));
                } catch (IOException e1) {
                    owner.close();
                    e1.printStackTrace();
                }
            });
            connectingTask.setOnFailed(e -> {
                progressStage.close();
                @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
                Throwable ex = e.getSource().getException();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Error");
                alert.setHeaderText("Error while connecting to the database");
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
                ex.printStackTrace();
            });

            new Thread(connectingTask).start();

            progressStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
