package controller;

import database.Database;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MainController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;

    @FXML
    private void connection(ActionEvent event) {
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



        //stage.setScene(new Scene(new Pane()));
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

    private void startProgressTask(Stage owner, Task<Void> connectingTask) {
        Group root = new Group();

        BorderPane mainPane = new BorderPane();
        mainPane.setPrefSize(350, 50);
        root.getChildren().add(mainPane);
        ProgressBar progressBar = new ProgressBar(-1.0);
        mainPane.setCenter(progressBar);

        Stage progressStage = new Stage();
        Scene progressScene = new Scene(root);

        progressStage.setScene(progressScene);
        progressStage.setTitle("Connecting to database");
        progressStage.initModality(Modality.APPLICATION_MODAL);
        progressStage.initOwner(owner);
        progressStage.setOnCloseRequest(e -> connectingTask.cancel());

        connectingTask.setOnSucceeded(e -> progressStage.hide());
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
    }
}
