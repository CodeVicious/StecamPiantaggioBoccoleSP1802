package stecamSP1802.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import stecamSP1802.ConfigurationManager;
import stecamSP1802.MainStecamPiantaggioBoccoleSP1802;

import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

public class PropertiesController implements Initializable, ControlledScreen {

    ConfigurationManager conf = ConfigurationManager.getInstance();

    final ObservableList<AppProperty> data = FXCollections.observableArrayList();

    ScreensController myController;

    @FXML
    TableView tableProperties;

    @FXML
    TableColumn tablePropertiesNome;

    @FXML
    TableColumn tablePropertiesVal;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tableProperties.setItems(data);
        tableProperties.setEditable(true);

        Properties prop = conf.getProp();
        tablePropertiesNome.setCellValueFactory(new PropertyValueFactory<AppProperty,String>("keyword"));
        tablePropertiesVal.setCellValueFactory(new PropertyValueFactory<AppProperty,String>("value"));
        Set<String> keys = prop.stringPropertyNames();

        for(String p: keys){
            data.add(new AppProperty(p,prop.getProperty(p)));
        }

        tablePropertiesVal.setCellFactory(TextFieldTableCell.forTableColumn());


    }

    @Override
    public void setScreenParent(ScreensController screenController) {
        myController = screenController;
    }

    public void onCancelProperties(ActionEvent event) {
        myController.setScreen(MainStecamPiantaggioBoccoleSP1802.mainID);
    }

    public void onSaveProperties(ActionEvent event) {
    }

    public void onEditSalva(TableColumn.CellEditEvent cellEditEvent) {
        AppProperty p = (AppProperty)cellEditEvent.getTableView().getItems()
                .get(cellEditEvent.getTablePosition().getRow());
        p.setValue((String)cellEditEvent.getNewValue());
        tableProperties.refresh();

    }
}
