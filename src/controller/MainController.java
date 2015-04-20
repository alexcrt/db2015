package controller;

import database.Database;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Model;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by alex on 20.04.15.
 */
public class MainController implements Initializable {

    private Connection con;

    @FXML
    private TableView<Model> dataTableView;
    @FXML
    private TextField keywordField;
    @FXML
    private ComboBox<String> tablesNameComboBox;
    @FXML
    private Button searchAnyTextButton;

    private final ObservableList<Model> tableViewList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            con = Database.getConnection();
            ObservableList<String> list = FXCollections.observableArrayList();
            String query = "SELECT table_name FROM user_tables";
            PreparedStatement preparedStatement = con.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                list.add(resultSet.getString(1));
            }
            resultSet.close();
            tablesNameComboBox.setItems(list);
            tablesNameComboBox.setValue(list.get(0));

            searchAnyTextButton.setOnAction(e -> searchFieldFromTable(tablesNameComboBox.getValue()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void searchFieldFromTable(String tableName) {
        if (keywordField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Search field cannot be empty");
            alert.setHeaderText("You need to provide a keyword");
            alert.showAndWait();
            return;
        }
        Task<Long> fetchingDatasTask = new Task<Long>() {
            @Override
            protected Long call() throws Exception {

                long nbRows = -1;
                try(ResultSet rs = con.prepareStatement("Select count(1) From " + tableName).executeQuery()) {
                    rs.next();
                    nbRows = rs.getLong(1);
                }

                try(ResultSet rs = con.prepareStatement("Select * From " + tableName + " WHERE ROWNUM <= 10").executeQuery()) {
                    ResultSetMetaData metaData = rs.getMetaData();

                    List<TableColumn<Model, String>> tableColumns = new ArrayList<>();
                    for (int i = 1; i <= metaData.getColumnCount(); i++) {
                        int index = i;
                        TableColumn<Model, String> column = new TableColumn<>(metaData.getColumnName(i));
                        column.setCellValueFactory(e -> new SimpleStringProperty(String.valueOf(e.getValue().getObject(index - 1))));
                        tableColumns.add(column);
                    }

                    //Update from UI Thread
                    Platform.runLater(() -> dataTableView.getColumns().setAll(tableColumns));

                    int num = 0;
                    tableViewList.clear();

                    while (rs.next()) {
                        updateMessage("Fetching row " + num++);
                        updateProgress(num, nbRows);
                        List<Object> res = new ArrayList<>();
                        for (int i = 1; i <= metaData.getColumnCount(); i++) {
                            res.add(rs.getObject(i));
                        }
                        tableViewList.add(new Model(res));
                    }
                    dataTableView.setItems(tableViewList);
                }

                return nbRows;
            }
        };

        Scene scene = (Scene) ((Button) searchAnyTextButton).getScene();
        Stage stage = (Stage) scene.getWindow();
        startFetchingTask(stage, fetchingDatasTask);
    }

    private void startFetchingTask(Stage owner, Task<Long> fetchingDatasTask) {
        try {
            Stage progressStage = new Stage();

            Parent root = FXMLLoader.load(getClass().getResource("../resources/progress_task.fxml"));
            Scene progressScene = new Scene(root);

            progressStage.setScene(progressScene);
            progressStage.setTitle("Fetching datas");
            progressStage.initModality(Modality.APPLICATION_MODAL);
            progressStage.initOwner(owner);
            progressStage.setOnCloseRequest(e -> fetchingDatasTask.cancel());


            fetchingDatasTask.setOnSucceeded(e -> progressStage.close());

            fetchingDatasTask.setOnFailed(e -> {
                progressStage.close();
                @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
                Throwable ex = e.getSource().getException();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Error");
                alert.setHeaderText("Error while fetching datas");
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
                ex.printStackTrace();
            });

            ProgressBar progressBar = (ProgressBar) progressScene.lookup("#progressBarTask");
            progressBar.progressProperty().bind(fetchingDatasTask.progressProperty());

            Label label = (Label) progressScene.lookup("#bottomLabel");
            label.textProperty().bind(fetchingDatasTask.messageProperty());

            Thread t = new Thread(fetchingDatasTask);
            t.setDaemon(true);
            t.start();

            progressStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
