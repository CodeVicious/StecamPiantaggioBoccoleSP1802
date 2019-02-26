package stecamSP1802.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import stecamSP1802.ConfigurationManager;

import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

public class LoginController implements Initializable, ControlledScreen {

    ScreensController myController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {


    }

    @Override
    public void setScreenParent(ScreensController screenController) {
        myController = screenController;
    }
}
