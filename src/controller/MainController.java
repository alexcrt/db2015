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
import model.PreComputedQueries;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by alex on 20.04.15.
 */
public class MainController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());
    private static final int ROWS_PER_PAGE = 10_000;

    private static final int MAX_ENTRIES = 50;

    private final Map<Integer, ObservableList<Model>> cache =
            new LinkedHashMap<Integer, ObservableList<Model>>(MAX_ENTRIES + 1, .75F, true) {
                public boolean removeEldestEntry(Map.Entry eldest) {
                    return size() > MAX_ENTRIES;
                }
            };

    private Connection con;

    private String lastTableNameSelected;
    private String lastKeywordSupplied;

    //Tab 1
    @FXML
    private TableView<Model> dataTableView;

    @FXML
    private Pagination paginationTab1;

    @FXML
    private TextField keywordField;
    @FXML
    private ComboBox<String> tablesNameComboBox;
    @FXML
    private Button searchAnyTextButton;

    //Tab 2
    @FXML
    private TableView<Model> queryResultsTableView;
    @FXML
    private ComboBox<PreComputedQueries> queryComboBox;
    @FXML
    private TextArea queryNameArea;
    @FXML
    private TextField inputTextField;
    
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

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    list.add(resultSet.getString(1));
                }
            }

            dataTableView.setPlaceholder(new Label("No datas in the table"));

            paginationTab1.setDisable(true);
            paginationTab1.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
                cache.putIfAbsent(oldIndex.intValue(), dataTableView.getItems());
                if (cache.containsKey(newIndex.intValue())) {
                    dataTableView.setItems(cache.get(newIndex.intValue()));
                } else {
                    searchFieldFromTable(tablesNameComboBox.getValue(), newIndex.intValue() * ROWS_PER_PAGE);
                }
            });

            queryResultsTableView.setPlaceholder(new Label("No datas in the table"));

            tablesNameComboBox.setItems(list);
            tablesNameComboBox.getSelectionModel().selectFirst();

            searchAnyTextButton.setOnAction(e -> {
                if (!tablesNameComboBox.getValue().equals(lastTableNameSelected) ||
                        !keywordField.getText().equals(lastKeywordSupplied)) {
                    lastTableNameSelected = tablesNameComboBox.getValue();
                    lastKeywordSupplied = keywordField.getText();

                    paginationTab1.setCurrentPageIndex(0);
                    paginationTab1.setDisable(true);

                    cache.clear();
                }
                searchFieldFromTable(tablesNameComboBox.getValue(), 0);
            });

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

                LOGGER.log(Level.INFO, query.getQuery());

                try (ResultSet rs = con.prepareStatement(query.getQuery()).executeQuery()) {
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

                    long num = 0;
                    queryResultsViewList.clear();

                    while (rs.next()) {
                        updateMessage("Fetching row " + num++);
                        List<Object> res = new ArrayList<>();
                        for (int i = 1; i <= metaData.getColumnCount(); i++) {
                            res.add(rs.getObject(i));
                        }
                        queryResultsViewList.add(new Model(res));
                    }
                    LOGGER.log(Level.INFO, "Fetched " + num + " rows");

                    queryResultsTableView.setItems(queryResultsViewList);
                }

                return null;
            }
        };

        Scene scene = (Scene) ((Button) executeQueryButton).getScene();
        Stage stage = (Stage) scene.getWindow();
        startFetchingTask(stage, fetchingDatasTask);
    }

    private void searchFieldFromTable(String tableName, int from) {
        if (cache.containsKey(from / ROWS_PER_PAGE)) {
            dataTableView.setItems(cache.get(from / ROWS_PER_PAGE));
            return;
        }
        Task<Void> fetchingDatasTask = keywordField.getText().isEmpty() ?
                taskAll(tableName, from) : taskWithKeyword(tableName, keywordField.getText(), from);


        Scene scene = (Scene) ((Button) searchAnyTextButton).getScene();
        Stage stage = (Stage) scene.getWindow();
        startFetchingTask(stage, fetchingDatasTask);
    }

    private Task<Void> taskAll(String tableName, int from) {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {

                String wantedQuery = "SELECT * FROM " + tableName + " ORDER BY ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

                PreparedStatement preparedStatement = con.prepareStatement(wantedQuery);
                preparedStatement.setInt(1, from);
                preparedStatement.setInt(2, ROWS_PER_PAGE);

                try (ResultSet rs = preparedStatement.executeQuery()) {
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

                    long num = 0;

                    final ObservableList<Model> tableViewList = FXCollections.observableArrayList();

                    String countQuery = "Select count(*) From " + tableName;

                    try (ResultSet countResultSet = con.prepareStatement(countQuery).executeQuery()) {
                        if (!countResultSet.next()) {
                            throw new SQLException("COUNT should give a value");
                        }
                        int nbPages = (int) (countResultSet.getLong(1) / ROWS_PER_PAGE) + 1;
                        Platform.runLater(() -> paginationTab1.setPageCount(nbPages));
                    }

                    while (rs.next()) {
                        updateMessage("Fetching row " + num++);
                        updateProgress(num, ROWS_PER_PAGE);
                        List<Object> res = new ArrayList<>();
                        for (int i = 1; i <= metaData.getColumnCount(); i++) {
                            if (metaData.getColumnType(i) == Types.CLOB) {
                                Clob clob = rs.getClob(i);
                                if (clob != null) {
                                    int length = clob.length() > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) clob.length();
                                    res.add(clob.getSubString(1L, length));
                                } else {
                                    res.add("null");
                                }
                            } else {
                                res.add(rs.getObject(i));
                            }
                        }
                        tableViewList.add(new Model(res));
                    }

                    LOGGER.log(Level.INFO, "Fetched " + num + " rows");

                    dataTableView.setItems(tableViewList);
                }

                return null;
            }
        };
    }

    private Task<Void> taskWithKeyword(String tableName, String keyword, int from) {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {

                updateMessage("Server - Preparing query");

                String placeHolder = "Select * From ";
                String buildingQuery = tableName + " WHERE";
                int limit = 0;

                PreparedStatement preparedStatement;
                try (ResultSet rsColumns = columnsForTable(tableName)) {

                    while (rsColumns.next()) {
                        buildingQuery += " " + rsColumns.getString(1) + " LIKE ? OR";
                        limit++;
                    }
                    buildingQuery = buildingQuery.substring(0, buildingQuery.length() - 3);

                    preparedStatement = con.prepareStatement(placeHolder + buildingQuery + "Order by ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

                    for (int i = 1; i <= limit; i++) {
                        preparedStatement.setString(i, "%" + keyword + "%");
                    }
                    preparedStatement.setInt(limit + 1, from);
                    preparedStatement.setInt(limit + 2, ROWS_PER_PAGE);

                }

                try (ResultSet rs = preparedStatement.executeQuery()) {
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

                    long num = 0;

                    final ObservableList<Model> tableViewList = FXCollections.observableArrayList();

                    String countQuery = "Select count(*) From " + buildingQuery;
                    PreparedStatement countPreparedStatement = con.prepareStatement(countQuery);

                    for (int i = 1; i <= limit; i++) {
                        countPreparedStatement.setString(i, "%" + keyword + "%");
                    }

                    try (ResultSet countResultSet = countPreparedStatement.executeQuery()) {
                        if (!countResultSet.next()) {
                            throw new SQLException("COUNT should give a value");
                        }
                        int nbPages = (int) (countResultSet.getLong(1) / ROWS_PER_PAGE + 1);
                        Platform.runLater(() -> paginationTab1.setPageCount(nbPages));
                    }

                    while (rs.next()) {
                        updateMessage("Fetching row " + num++);
                        updateProgress(num, ROWS_PER_PAGE);
                        List<Object> res = new ArrayList<>();
                        for (int i = 1; i <= metaData.getColumnCount(); i++) {
                            if (metaData.getColumnType(i) == Types.CLOB) {
                                Clob clob = rs.getClob(i);
                                if (clob != null) {
                                    int length = clob.length() > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) clob.length();
                                    res.add(clob.getSubString(1L, length));
                                } else {
                                    res.add("null");
                                }
                            } else {
                                res.add(rs.getObject(i));
                            }
                        }
                        tableViewList.add(new Model(res));
                    }

                    LOGGER.log(Level.INFO, "Fetched " + num + " rows");

                    dataTableView.setItems(tableViewList);
                }

                return null;
            }
        };
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


            fetchingDatasTask.setOnSucceeded(e -> {
                progressStage.close();
                paginationTab1.setDisable(false);
            });

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