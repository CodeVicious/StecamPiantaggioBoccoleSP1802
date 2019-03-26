package stecamSP1802.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import stecamSP1802.ConfigurationManager;
import stecamSP1802.MainStecamPiantaggioBoccoleSP1802;
import stecamSP1802.helper.PasswordMD5Converter;

import java.net.URL;
import java.util.*;

public class PropertiesController implements Initializable, ControlledScreen {

    ConfigurationManager conf = ConfigurationManager.getInstance();

    final ObservableList<AppProperty> data = FXCollections.observableArrayList();

    ScreensController myController;

    @FXML
    TableView<AppProperty> tableProperties;

    @FXML
    TableColumn<AppProperty, String> tablePropertiesNome;

    @FXML
    TableColumn<AppProperty, String> tablePropertiesVal;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tableProperties.setItems(data);
        tableProperties.setEditable(true);

        Map<String, String> propDB = conf.getPropDB();
        Properties propFILE = conf.getPropFile();
        tablePropertiesNome.setCellValueFactory(new PropertyValueFactory<AppProperty, String>("keyword"));
        tablePropertiesVal.setCellValueFactory(new PropertyValueFactory<AppProperty, String>("value"));


        Set<String> keysDB = propDB.keySet();
        Set<String> keysF = propFILE.stringPropertyNames();

        for (String p : keysF) {
            data.add(new AppProperty(p, propFILE.getProperty(p)));
        }

        for (String p : keysDB) {
            if(!p.matches("password-Amministratore")){
                data.add(new AppProperty(p, propDB.get(p)));
            } else{
                data.add(new AppProperty(p, mask(propDB.get(p))));
            }
        }

        tablePropertiesVal.setCellFactory(TextFieldTableCell.forTableColumn());


    }

    private String mask(String text) {
        char[] chars = new char[text.length()];
        Arrays.fill(chars, '*');
        return new String(chars);
    }

    @Override
    public void setScreenParent(ScreensController screenController) {
        myController = screenController;
    }

    public void onCancelProperties(ActionEvent event) {
        myController.setScreen(MainStecamPiantaggioBoccoleSP1802.mainID);
    }

    public void onSaveProperties(ActionEvent event) {

        conf.saveProperties();
        myController.setScreen(MainStecamPiantaggioBoccoleSP1802.mainID);

    }

    public void onEditCommit(TableColumn.CellEditEvent cellEditEvent) {
        AppProperty p = (AppProperty) cellEditEvent.getTableView().getItems()
                .get(cellEditEvent.getTablePosition().getRow());

        boolean isAmmPwd = p.getKeyword().matches("password-Amministratore");
        if(!isAmmPwd) {
            p.setValue((String) cellEditEvent.getNewValue());
        } else {
            p.setValue(mask((String) cellEditEvent.getNewValue()));
        }

        if (conf.getPropDB().get(p.getKeyword()) != null) {
            if(!isAmmPwd) {
                conf.getPropDB().put(p.getKeyword(), p.getValue());
            } else {
                conf.getPropDB().put(p.getKeyword(), PasswordMD5Converter.getMD5((String) cellEditEvent.getNewValue()));
            }
        } else {
            conf.getPropFile().setProperty(p.getKeyword(), p.getValue());
        }

        tableProperties.refresh();

    }


}
