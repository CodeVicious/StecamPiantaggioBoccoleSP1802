package stecamSP1802.controllers;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import stecamSP1802.ConfigurationManager;
import stecamSP1802.MainStecamPiantaggioBoccoleSP1802;
import stecamSP1802.services.DbService;
import stecamSP1802.services.StatusManager;
import stecamSP1802.services.WebQueryService;

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
            if (pwLBL.get() != null)
                pwLBL.set(pwLBL.get() + "*");
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
                checkMatricola(matricola);
            } catch (SQLException e) {
                Logger.error(e);
            }
        } else { //Check Password
            if (password.length() == 0) {
                setMsg("PW VUOTA - INSERIRE PASSWORD");
                return;
            }
            try {
                checkPassword(password);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


    }

    private void checkPassword(StringBuilder password) throws SQLException {
            ResultSet rs = dbService.queryMatricolaPassword(matricola, password);
            if (!rs.isBeforeFirst()) {
                setMsg(matricola + " PASSWORD SBAGLIATA! - RIPETERE PASSWORD");
                password.setLength(0);
                pwLBL.set("");
            } else {
                rs.next();
                loggedIn(rs.getString("NomeOperatore"), true, true);
            }


    }

    private void checkMatricola(StringBuilder matricola) throws SQLException {
            ResultSet rs = dbService.queryMatricola(matricola);
            if (!rs.next()) {
                setMsg("MATRICOLA NON PRESENTE! - REINSERIRE");
                cleanUP();
            } else {
                isMastricolaStage = false; //eventuale password stage

                if (rs.getBoolean("ConduttoreDiLinea")) { //Conduttore di Linea quindi password
                    setMsg(rs.getString("NomeOperatore") + " INSERIRE PASSWORD");
                } else { //NON Conduttore di linea quindi procedere.
                    loggedIn(rs.getString("NomeOperatore"), false, true);
                }
            }
    }

    private void loggedIn(String nomeOperatore, boolean isConduttoreDiLinea, boolean isOnLine) {
        MainController m = (MainController) myController.getController(MainStecamPiantaggioBoccoleSP1802.mainID);

        if(!isConduttoreDiLinea && WebQueryService.getInstance().isWebOffline()){
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    "ATTENZIONE WEB OFFLINE INSERIRE ACCOUNT AMMINISTRATORE", ButtonType.OK);
            alert.showAndWait();
            cleanUP();
            setMsg("");
        } else{
            m.setLoggedUser(matricola.toString(), password.toString(), nomeOperatore, isConduttoreDiLinea, isOnLine);
            cleanUP();
            setMsg("");
            m.startBarCodeService(); //Inizializzo il BarCode solo a Login ok
            myController.setScreen(MainStecamPiantaggioBoccoleSP1802.mainID);
        }

        m.startBarCodeService();
    }

    public void onRESETPressed(ActionEvent event) {
        cleanUP();
    }

    private void cleanUP() {
        isMastricolaStage = true;
        matricola.setLength(0);
        password.setLength(0);
        matrLBL.set(String.valueOf(matricola));
        pwLBL.set("");
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

    public void setOFFLINEControls() {
        isMastricolaStage = true;
        setMsg("INSERIRE CREDENZIALI ");
    }

    public String getPassword() {
        return password.toString();
    }

    public String getMatricola() {
        return matricola.toString();
    }
}
