package controller;

import database.Database;
import javafx.beans.value.ObservableListValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Observable;
import java.util.ResourceBundle;

/**
 * Created by alex on 20.04.15.
 */
public class MainController implements Initializable {

    private Connection con;

    @FXML private ComboBox<String> tablesNameComboBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            con = Database.getConnection();
            ObservableList<String> list = FXCollections.observableArrayList();
            String query = "SELECT table_name FROM user_tables";
            PreparedStatement preparedStatement = con.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()) {
                list.add(resultSet.getString(1));
            }
            resultSet.close();
            tablesNameComboBox.setItems(list);
            tablesNameComboBox.setValue(list.get(0));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
