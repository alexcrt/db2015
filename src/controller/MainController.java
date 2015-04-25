package controller;

import database.Database;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.adapter.JavaBeanStringPropertyBuilder;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableStringValue;
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
import model.PreComputedQueries;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.stream.Collectors.toList;

/**
 * Created by alex on 20.04.15.
 */
public class MainController implements Initializable {

    private static final Logger logger = Logger.getLogger(MainController.class.getName());

    private Connection con;

    //Tab 1
    @FXML
    private TableView<Model> dataTableView;
    @FXML
    private TextField keywordField;
    @FXML
    private ComboBox<String> tablesNameComboBox;
    @FXML
    private Button searchAnyTextButton;

    private final ObservableList<Model> tableViewList = FXCollections.observableArrayList();

    //Tab 2
    @FXML
    private TableView<Model> queryResultsTableView;
    @FXML
    private ComboBox<PreComputedQueries> queryComboBox;
    @FXML
    private TextArea queryNameArea;
    @FXML
    private Button executeQueryButton;

    private final ObservableList<Model> queryResultsViewList = FXCollections.observableArrayList();

    private final List<PreComputedQueries> preComputedQueries = Arrays.asList(PreComputedQueries.values());

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
            tablesNameComboBox.getSelectionModel().selectFirst();

            searchAnyTextButton.setOnAction(e -> searchFieldFromTable(tablesNameComboBox.getValue()));

            queryComboBox.setItems(FXCollections.observableArrayList(preComputedQueries));

            SelectionModel<PreComputedQueries> selectionModel = queryComboBox.getSelectionModel();
            queryComboBox.setOnAction(v -> queryNameArea.setText(selectionModel.getSelectedItem().getQuery()));
            selectionModel.selectFirst();
            queryNameArea.setText(selectionModel.getSelectedItem().getQuery());

            executeQueryButton.setOnAction(e -> executePreComputedQuery(selectionModel.getSelectedItem()));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void executePreComputedQuery(PreComputedQueries query) {

        Task<Void> fetchingDatasTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {

                logger.log(Level.INFO, query.getQuery());

                try(ResultSet rs = con.prepareStatement(query.getQuery()).executeQuery()) {
                    ResultSetMetaData metaData = rs.getMetaData();

                    List<TableColumn<Model, String>> tableColumns = new ArrayList<>();
                    for (int i = 1; i <= metaData.getColumnCount(); i++) {
                        int index = i;
                        TableColumn<Model, String> column = new TableColumn<>(metaData.getColumnName(i));
                        column.setCellValueFactory(e -> new SimpleStringProperty(String.valueOf(e.getValue().getObject(index - 1))));
                        tableColumns.add(column);
                    }

                    //Update from UI Thread
                    Platform.runLater(() -> queryResultsTableView.getColumns().setAll(tableColumns));

                    int num = 0;
                    queryResultsViewList.clear();

                    while (rs.next()) {
                        updateMessage("Fetching row " + num++);
                        List<Object> res = new ArrayList<>();
                        for (int i = 1; i <= metaData.getColumnCount(); i++) {
                            res.add(rs.getObject(i));
                        }
                        queryResultsViewList.add(new Model(res));
                    }
                    queryResultsTableView.setItems(queryResultsViewList);
                }

                return null;
            }
        };

        Scene scene = (Scene) ((Button) executeQueryButton).getScene();
        Stage stage = (Stage) scene.getWindow();
        startFetchingTask(stage, fetchingDatasTask);
    }

    private void searchFieldFromTable(String tableName) {
        if (keywordField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Search field cannot be empty");
            alert.setHeaderText("You need to provide a keyword");
            alert.showAndWait();
            return;
        }
        Task<Void> fetchingDatasTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {

                String wantedQuery = "Select * From " + tableName + " WHERE";

                PreparedStatement preparedStatement;
                try(ResultSet rsColumns = columnsForTable(tableName)) {
                    String keyword = keywordField.getText();

                    int limit = 0;
                    while (rsColumns.next()) {
                        wantedQuery += " " + rsColumns.getString(1) + " LIKE ? OR";
                        limit++;
                    }
                    wantedQuery = wantedQuery.substring(0, wantedQuery.length()-3);

                    preparedStatement = con.prepareStatement(wantedQuery);

                    for(int i = 1; i <= limit; i++) {;
                        preparedStatement.setString(i, "'%"+keyword+"%'");
                    }
                }

                try(ResultSet rs = preparedStatement.executeQuery()) {
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
                        //updateProgress(num, nbRows);
                        List<Object> res = new ArrayList<>();
                        for (int i = 1; i <= metaData.getColumnCount(); i++) {
                            res.add(rs.getObject(i));
                        }
                        tableViewList.add(new Model(res));
                    }
                    dataTableView.setItems(tableViewList);
                }

                return null;
            }
        };

        Scene scene = (Scene) ((Button) searchAnyTextButton).getScene();
        Stage stage = (Stage) scene.getWindow();
        startFetchingTask(stage, fetchingDatasTask);
    }

    private ResultSet columnsForTable(String tableName) throws SQLException {
        String query = "Select column_name From USER_TAB_COLUMNS WHERE table_name = ?";
        PreparedStatement preparedStatement = con.prepareStatement(query);
        preparedStatement.setString(1, tableName);
        return preparedStatement.executeQuery();
    }

    private void startFetchingTask(Stage owner, Task<Void> fetchingDatasTask) {
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
