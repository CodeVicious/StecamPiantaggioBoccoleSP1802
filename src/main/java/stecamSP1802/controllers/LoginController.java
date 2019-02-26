package stecamSP1802.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.ObservableValueBase;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable, ControlledScreen {

    ScreensController myController;

    StringBuilder matricola = new StringBuilder("");

    @FXML
    private Button tastoUNO;

    @FXML
    private Button tastoOTTO;

    @FXML
    private Button tasto4;

    @FXML
    private Button tasto9;

    @FXML
    private Button tasto2;

    @FXML
    private Button tasto1;

    @FXML
    private Button tasto6;

    @FXML
    private Button tasto5;

    @FXML
    private Button tastoOK;

    @FXML
    private Button tasto0;

    @FXML
    private Button tastoCANCEL;

    @FXML
    private Button tasto3;

    @FXML
    private Label matriLBL;

    @FXML
    private Label loginMSG;


    SimpleStringProperty fname = new SimpleStringProperty();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        matriLBL.textProperty().bind(fname);

    }

    @Override
    public void setScreenParent(ScreensController screenController) {
        myController = screenController;
    }

    public void onKeyPressed(ActionEvent event) {
        matricola.append(((Button)event.getSource()).getText());
        fname.set(String.valueOf(matricola));
        System.out.println(matricola);
    }

    public void onOKPressed(ActionEvent event) {
        if(matricola.length()==0) {
            loginMSG.setText("MATRICOLA VUOTA");
            return;
        }


    }

    public void onRESETPressed(ActionEvent event) {
        matricola.setLength(0);
        fname.set(String.valueOf(matricola));
    }
}
