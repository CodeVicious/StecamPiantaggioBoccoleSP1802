package stecamSP1802.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoginPopUpController extends AbstractController  {
    private static Logger Logger = LogManager.getLogger(LoginPopUpController.class);
    @FXML
    private Button tasto1;

    @FXML
    private Button tasto8;

    @FXML
    private Button tasto4;

    @FXML
    private Button tasto9;

    @FXML
    private Button tasto2;

    @FXML
    private Button tasto6;

    @FXML
    private Button tasto5;

    @FXML
    private Button tastoOK;

    @FXML
    private Button tasto0;

    @FXML
    private Button tastoRESET;

    @FXML
    private Button tasto3;

    private Stage stage;

    StringBuilder password = new StringBuilder("");

    public void setStage(Stage popupStage) {
        this.stage = popupStage;
    }

    public void onKeyPressed(ActionEvent event) {
        password.append(((Button) event.getSource()).getText());
    }

    public void onOKPressed(ActionEvent event) {

        closeStage();
    }

    public void onRESETPressed(ActionEvent event) {
        password.setLength(0);
        password.append("CANCEL");
        closeStage();
    }

    private void closeStage() {
        if(stage!=null) {
            stage.close();
        }
    }

    public String getPassword() {
        return password.toString();
    }
}
