package stecamSP1802.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import stecamSP1802.MainStecamPiantaggioBoccoleSP1802;

import java.net.URL;
import java.util.ResourceBundle;

public class PropertiesController implements Initializable, ControlledScreen {

    ScreensController myController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

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
    }
}
