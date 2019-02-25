package stecamSP1802.controllers;

import javafx.fxml.Initializable;
import stecamSP1802.controllers.ControlledScreen;

import java.net.URL;
import java.util.ResourceBundle;

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
