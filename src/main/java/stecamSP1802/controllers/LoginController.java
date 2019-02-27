package stecamSP1802.controllers;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import stecamSP1802.MainStecamPiantaggioBoccoleSP1802;
import stecamSP1802.services.DbService;
import stecamSP1802.services.StatusManager;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;


public class LoginController implements Initializable, ControlledScreen {
    private static Logger Logger = LogManager.getLogger(LoginController.class);

    @FXML
    private Button tasto1;

    @FXML
    private Button tasto2;

    @FXML
    private Button tasto3;

    @FXML
    private Button tasto4;

    @FXML
    private Button tasto5;

    @FXML
    private Button tasto6;

    @FXML
    private Button tasto7;

    @FXML
    private Button tasto8;

    @FXML
    private Button tasto9;


    @FXML
    private Button tastoOK;

    @FXML
    private Button tasto0;

    @FXML
    private Button tastoCANCEL;


    @FXML
    private Label matriLBL;

    @FXML
    private Label pwFIELD;

    @FXML
    private Label loginMSG;

    @FXML
    private Label remoteDBStatus;



    ScreensController myController;

    StatusManager statusManager;
    DbService dbService;

    StringBuilder matricola = new StringBuilder("");
    StringBuilder password = new StringBuilder("");
    ;

    SimpleStringProperty matrLBL = new SimpleStringProperty();
    SimpleStringProperty pwLBL = new SimpleStringProperty();


    private boolean isMastricolaStage = true;

    public void setStatusManager(StatusManager statusManager) {
        this.statusManager = statusManager;
    }

    public void setDbService(DbService dbService) {
        this.dbService = dbService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        matriLBL.textProperty().bind(matrLBL);
        pwFIELD.textProperty().bind(pwLBL);

    }

    @Override
    public void setScreenParent(ScreensController screenController) {
        myController = screenController;
    }

    public void onKeyPressed(ActionEvent event) {
        if (isMastricolaStage) {
            matricola.append(((Button) event.getSource()).getText());
            matrLBL.set(String.valueOf(matricola));
            System.out.println(matricola);
        } else {
            password.append(((Button) event.getSource()).getText());
            if(pwLBL.get()!=null)
                pwLBL.set(pwLBL.get()+"*");
            else
                pwLBL.set("*");
        }
    }

    public void onOKPressed(ActionEvent event) {
        if (isMastricolaStage) { //Fase di inserimento matricola
            if (matricola.length() == 0) {
                loginMSG.setText("MATRICOLA VUOTA");
                return;
            }

            try {
                ResultSet rs = dbService.queryMatricola(matricola);
                if (!rs.isBeforeFirst()) {
                    setMsg("MATRICOLA NON PRESENTE! - REINSERIRE");
                    cleanUP();
                } else {
                    isMastricolaStage = false; //eventuale password stage
                    rs.next();
                    if (rs.getBoolean("ConduttoreDiLinea")) { //Conduttore di Linea quindi password
                        setMsg(rs.getString("NomeOperatore") + " INSERIRE PASSWORD");
                    } else { //NON Conduttore di linea quindi procedere.
                        MainController m = (MainController) myController.getController(MainStecamPiantaggioBoccoleSP1802.mainID);
                        m.setLoggedUser(matricola.toString(), rs.getString("NomeOperatore"), false);
                        myController.setScreen(MainStecamPiantaggioBoccoleSP1802.mainID);
                    }
                }
            } catch (SQLException e) {
                Logger.error(e);
            }
        } else {
            if (password.length() == 0) {
                setMsg("PW VUOTA - INSERIRE PASSWORD");
                return;
            }
            try {
                ResultSet rs = dbService.queryMatricolaPassword(matricola, password);
                if (!rs.isBeforeFirst()) {
                    setMsg(matricola + " PASSWORD SBAGLIATA! - RIPETERE PASSWORD");
                    password.setLength(0);
                    pwLBL.set("");
                } else {
                    rs.next();
                    MainController m = (MainController) myController.getController(MainStecamPiantaggioBoccoleSP1802.mainID);
                    m.setLoggedUser(matricola.toString(), rs.getString("NomeOperatore"), true);
                    cleanUP();
                    myController.setScreen(MainStecamPiantaggioBoccoleSP1802.mainID);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void onRESETPressed(ActionEvent event) {
        cleanUP();
    }

    private void cleanUP() {
        isMastricolaStage = true;
        matricola.setLength(0);
        password.setLength(0);
        matrLBL.set(String.valueOf(matricola));
    }

    private void setMsg(String msg) {
        loginMSG.setText(msg);
    }

    public void onDbConnected() {
        Platform.runLater(() -> {
            remoteDBStatus.setText("GLOBAL DB - CONNECTED");
            remoteDBStatus.setStyle("-fx-background-color: green");
        });
    }

    public void onDbDISConnected() {
        Platform.runLater(() -> {
            remoteDBStatus.setText("GLOBAL DB - DISCONNECTED");
            remoteDBStatus.setStyle("-fx-background-color: red");
        });

    }
}
