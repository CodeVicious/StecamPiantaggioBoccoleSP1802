package stecamSP1802.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import stecamSP1802.ConfigurationManager;
import stecamSP1802.MainStecamPiantaggioBoccoleSP1802;
import stecamSP1802.helper.PasswordMD5Converter;
import stecamSP1802.schedulers.WatchDog;
import stecamSP1802.services.StatusManagerListenerImp;
import stecamSP1802.services.WebQueryService;
import stecamSP1802.services.*;
import stecamSP1802.services.barcode.Parte;
import stecamSP1802.services.barcode.SerialService;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainController extends AbstractController implements Initializable, ControlledScreen {
    private static Logger Logger = LogManager.getLogger(MainController.class);

    ScreensController myController;

    //Controlli interfaccia
    @FXML
    Label stecamTime;

    @FXML
    TextField barcodeWO;

    @FXML
    TextField codiceRICETTA;

    @FXML
    Label msgBOX;

    @FXML
    Label plcStatus;

    @FXML
    Label remoteDBStatus;

    @FXML
    Label localDBStatus;

    @FXML
    Label labelESITO;


    @FXML
    Label lblUtenteLoggato;

    @FXML
    CheckBox controlloWO;

    @FXML
    CheckBox controlloUDM;

    @FXML
    private Button interfacciaParametri;

    @FXML
    private Button synckUSERS;

    @FXML
    private Label cicloDESCRIZIONE;

    @FXML
    private Label cicloWO;

    @FXML
    private Label cicloPRG;

    @FXML
    private TableView woTblPiantaggio;

    @FXML
    private TableColumn tblArticolo;

    @FXML
    private TableColumn tblDescrizione;

    @FXML
    private TableColumn tblCheck;


    final ObservableList<WOTable> tblWoData = FXCollections.observableArrayList();

    //Servizi
    ExecutorService executors;
    final ConfigurationManager conf = ConfigurationManager.getInstance();
    final LoggedUser loggedUser = LoggedUser.getInstance();

    StatusManager statusManager;
    DbService dbService;
    SerialService serialService;
    PlcService plcService;
    WebQueryService webQueryService;
    WatchDog watchDog;

    // Observers
    PLCListener plcListener;
    StatusManagerListener statusManagerListener;


    private long minute;
    private long second;
    private int hour;
    private String matricola;
    private String nomeOperatore;
    private boolean isConduttoreDiLinea;
    private boolean isWOListPartEnabled = true;
    private boolean isUDMVerificaEnabled = true;


    @Override
    public void setScreenParent(ScreensController screenController) {
        myController = screenController;
    }


    public void initialize(URL location, ResourceBundle resources) {

        // Setup TableView

        woTblPiantaggio.setItems(tblWoData);
        woTblPiantaggio.setEditable(false);

        tblArticolo.setCellValueFactory(new PropertyValueFactory<WOTable, String>("articolo"));
        tblDescrizione.setCellValueFactory(new PropertyValueFactory<WOTable, String>("descrizioneDett"));
        tblCheck.setCellValueFactory(new PropertyValueFactory<WOTable, String>("check"));


        //Setup Thread Pool for PLC Service
        executors = Executors.newCachedThreadPool();

        statusManagerListener = new StatusManagerListenerImp(this);
        statusManager = new StatusManager(); // Gestore degli stati generale e di connessione
        statusManager.addListener(statusManagerListener);
        webQueryService = new WebQueryService(statusManager);
        serialService = new SerialService(this, statusManager, webQueryService); //Gestore Bar Code
        dbService = new DbService(statusManager); //Gestore interfacce DB
        watchDog = new WatchDog(this); // Gestore inattività ed altri alert temporizzati.

        plcListener = new PLCListenerImp(this, statusManager);
        plcService = new PlcService(
                conf.getPlcName(),
                conf.getPlcIP(),
                new byte[conf.getByteArrayPlcPc()],
                new byte[conf.getByteArrayPcPlc()],
                conf.getDbNumberPlcPc(),
                conf.getDbNumberPcPlc(),
                conf.getBitMonitor(),
                plcListener,
                statusManager,
                webQueryService,
                executors
        ); // Gestore interfaccia PLC

        launchTime(); //Clock TODO:REMOVE?
    }

    public void startMainServices() {
        dbService.connectDB();
        plcService.connect();
    }

    public void startBarCodeService() {
        serialService.open();
    }

    private void launchTime() {

        Task task = new Task<Void>() {
            @Override
            public Void call() {
                for (; ; ) {
                    DateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                    Calendar cal = Calendar.getInstance();

                    second = cal.get(Calendar.SECOND);
                    minute = cal.get(Calendar.MINUTE);
                    hour = cal.get(Calendar.HOUR);
                    //System.out.println(hour + ":" + (minute) + ":" + second);
                    Platform.runLater(new Runnable() {
                        public void run() {
                            stecamTime.setText(hour + ":" + (minute) + ":" + second);
                        }
                    });


                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                    }
                }
            }
        };

        new Thread(task).start();


    }


    public void CloseApp() {
        plcService.closeConnection();
        //dbService.close();
        //serialService.close();
        Platform.exit();
        System.exit(0);
    }

    public void plcDisconnected() {
        Logger.warn("PLC DISCONNECTED ");
        Platform.runLater(() -> {
            statusManager.setGlobalStatus(StatusManager.GlobalStatus.CONNECTING);
            plcStatus.setText("PLC - DISCONNECTED");
            plcStatus.setStyle("-fx-background-color: red");
        });
        showMesage("PLC DISCONNECTED ");
    }

    public void plcConnected() {
        Logger.info("PLC CONNECTED ");
        showMesage("PLC CONNECTED ");
        Platform.runLater(() -> {
            plcStatus.setText("PLC - CONNECTED");
            plcStatus.setStyle("-fx-background-color: green");
            if (statusManager.getLocalDbStatus() == StatusManager.LocalDbStatus.LOCAL_DB_CONNECTED)
                statusManager.setGlobalStatus(StatusManager.GlobalStatus.WAITING_WO);
        });
    }

    public void gDbDisconnected() {
        Logger.warn("SQL DB SERVER DISCONNECTED ");
        Platform.runLater(() -> {
            remoteDBStatus.setText("GLOBAL DB - DISCONNECTED");
            remoteDBStatus.setStyle("-fx-background-color: red");

            //Comunico alla finestra di Login che il db remoto è disconnesso
            ((LoginController) myController.getController(MainStecamPiantaggioBoccoleSP1802.loginID)).onDbDISConnected();
        });
        showMesage("SQL DB SERVER DISCONNECTED ");
    }

    public void gDbConnected() {
        Logger.info("SQL DB SERVER CONNECTED ");
        Platform.runLater(() -> {
            remoteDBStatus.setText("GLOBAL DB - CONNECTED");
            remoteDBStatus.setStyle("-fx-background-color: green");

            //Comunico alla finestra di Login che il db remoto è disconnesso
            ((LoginController) myController.getController(MainStecamPiantaggioBoccoleSP1802.loginID)).onDbConnected();
        });
        showMesage("SQL DB SERVER CONNECTED ");
    }

    public void lDbDisconnected() {
        Logger.warn("LOCAL SQL DB SERVER DISCONNECTED ");
        Platform.runLater(() -> {
            statusManager.setGlobalStatus(StatusManager.GlobalStatus.CONNECTING);
            localDBStatus.setText("LOCAL DB - DISCONNECTED");
            localDBStatus.setStyle("-fx-background-color: red");

        });
        showMesage("LOCAL SQL DB SERVER DISCONNECTED ");
    }

    public void lDbConnected() {
        Logger.info("LOCAL SQL DB SERVER CONNECTED ");
        Platform.runLater(() -> {
            localDBStatus.setText("LOCAL DB - CONNECTED");
            localDBStatus.setStyle("-fx-background-color: green");
            if (statusManager.getPlcStatus() == StatusManager.PlcStatus.PLC_CONNECTED)
                statusManager.setGlobalStatus(StatusManager.GlobalStatus.WAITING_WO);
        });
        showMesage("LOCAL SQL DB SERVER CONNECTED");
    }

    public void onRicettaOK() {
        plcService.unsetRicettaCaricata(); //Abbasso il bit di carica ricetta al PLC

        Logger.warn("RICETTA CARICATA! ");
        Platform.runLater(() -> {
            codiceRICETTA.setText(webQueryService.getWO().getCodiceRicetta());
            codiceRICETTA.setStyle("-fx-background-color: green");
            cicloWO.setText(webQueryService.getWO().getBarCodeWO());
            cicloPRG.setText(webQueryService.getWO().getCodiceRicetta());
            cicloDESCRIZIONE.setText(webQueryService.getWO().getDescrizione());

            Map<String, Parte> lista = webQueryService.getParti();
            for (String art : lista.keySet()) {
                tblWoData.add(new WOTable(lista.get(art).getCodice(), lista.get(art).getDescrizione(), lista.get(art).getVerificato()));
            }

            statusManager.setGlobalStatus(StatusManager.GlobalStatus.WAITING_UDM);
        });

        showMesage("RICETTA CARICATA! ");
    }

    public void onRicettaKO() {
        plcService.unsetRicettaCaricata();
        Logger.warn("RICETTA KO! ");
        Platform.runLater(() -> {
            codiceRICETTA.setText(webQueryService.getWO().getCodiceRicetta() + " NON PRESENTE ");
            codiceRICETTA.setStyle("-fx-background-color: red");
            statusManager.setGlobalStatus(StatusManager.GlobalStatus.WAITING_WO);
        });
        showMesage("RICETTA KO! Ripetere il caricamento WorkOrder con codiceDett UDM corretti");
    }

    public void piantaggioBUONO() {
        Platform.runLater(() -> {
            labelESITO.setText("BUONO");
            labelESITO.setStyle("-fx-background-color: green");
        });
        Logger.warn("PIANTAGGIO BUONO! ");
        showMesage("PIANTAGGIO BUONO! ");

        dbService.storePiantaggio(loggedUser.getMatricola(), codiceRICETTA.getText(), barcodeWO.getText(), "OK");
    }

    public void piantaggioSCARTO() {
        Platform.runLater(() -> {
            labelESITO.setText("SCARTO");
            labelESITO.setStyle("-fx-background-color: red");

        });
        Logger.warn("PIANTAGGIO SCARTO! ");
        showMesage("PIANTAGGIO SCARTO! ");

        dbService.storePiantaggio(loggedUser.getMatricola(), codiceRICETTA.getText(), barcodeWO.getText(), "KO");
    }

    public void onNewBarCode(String barCode) {

        switch (statusManager.getGlobalStatus()) {

            case WAITING_WO:
                Platform.runLater(new Runnable() {
                    public void run() {
                        //check BarCode
                        if (!barCode.matches("\\d{7,8}")) {
                            Logger.error("Il BarCode " + barCode + " NON E' UN VALIDO WORK ORDER");
                            showMesage("CODICE WO - ERRATO");
                            barcodeWO.setText(barCode);
                            barcodeWO.setStyle("-fx-control-inner-background: red");
                        } else {
                            barcodeWO.setText(barCode);
                            barcodeWO.setStyle("-fx-control-inner-background: green");
                            cicloWO.setText(barCode);

                            if (isWOListPartEnabled) {
                                String ricetta = webQueryService.VerificaListaPartiWO(barCode);
                                plcService.sendCodiceRicetta(ricetta);
                            } else {
                                showMesage("Inserire il Codice Ricetta");
                                statusManager.setGlobalStatus(StatusManager.GlobalStatus.WAITING_CODICE_RICETTA);
                            }
                        }
                    }
                });
                break;
            case WAITING_CODICE_RICETTA:
                Platform.runLater(new Runnable() {
                    public void run() {
                        if (!barCode.matches("\\d{8}[A-Z]?")) {
                            Logger.error("Il BarCode " + barCode + " NON E' UN VALIDO CODICE ARTICOLO");
                            showMesage("CODICE WO - ERRATO");
                            codiceRICETTA.setText(barCode);
                            codiceRICETTA.setStyle("-fx-control-inner-background: red");

                        } else {
                            codiceRICETTA.setText(barCode);
                            codiceRICETTA.setStyle("-fx-control-inner-background: green");
                            codiceRICETTA.setText("disabled"); //Chiedo il caricamento della ricetta direttamente al PLC
                            plcService.sendCodiceRicetta(barCode);
                        }
                    }
                });
                break;
            case WAITING_UDM:
                Platform.runLater(new Runnable() {
                    public void run() {
                        //check BarCode
                        if (!barCode.matches("\\d{4}(?i)(99|CS|EM|MM|MV|NQ|PI|PR|UC|UE|US)\\d{5,8}")) {
                            Logger.error("Il BarCode " + barCode + " NON E' UN VALIDO UDM CODE");
                            showMesage("CODICE UdM - ERRATO");
                        } else {
                            if (isUDMVerificaEnabled) {
                                if (webQueryService.VerificaUDM(barCode, isWOListPartEnabled)) {
                                    refreshTabellaWO();
                                    if (webQueryService.checkValidazioneUDM()) {
                                        statusManager.setGlobalStatus(StatusManager.GlobalStatus.WORKING);
                                        plcService.iniziaCicloMacchina();
                                    }
                                    addTabellaWO(barCode);
                                } else {
                                    Logger.error("Il BarCode " + barCode + " NON E' NELLA LISTA COMPONENTI");
                                    showMesage("COMPONENTE NON PRESENTE NELLA LISTA");
                                }
                            } else {
                                showMesage("Inserire il Codice Componente");
                                statusManager.setGlobalStatus(StatusManager.GlobalStatus.WAITING_CODICE_COMPONENTE);
                            }
                        }
                    }
                });
                break;
            case WAITING_CODICE_COMPONENTE:
                Platform.runLater(new Runnable() {
                    public void run() {
                        //check BarCode
                        if (!barCode.matches("\\d{8}[A-Z]?")) {
                            Logger.error("Il BarCode " + barCode + " NON E' UN VALIDO CODICE COMPONENTE");
                            showMesage("CODICE COMPONENTE- ERRATO");
                        } else {
                            if (VerificaCodice(barCode)) {
                                refreshTabellaWO();
                                if (checkValidazioneUDM()) {
                                    statusManager.setGlobalStatus(StatusManager.GlobalStatus.WORKING);
                                    plcService.iniziaCicloMacchina();
                                } else {
                                    showMesage("Inserire il Codice UdM");
                                    statusManager.setGlobalStatus(StatusManager.GlobalStatus.WAITING_UDM);
                                }
                            } else {
                                Logger.error("Il BarCode " + barCode + " NON E' NELLA LISTA COMPONENTI");
                                showMesage("COMPONENTE NON PRESENTE NELLA LISTA");
                            }
                        }
                    }
                });
                break;

        }

    }

    private boolean checkValidazioneUDM() {
        return true;
    }

    private boolean VerificaCodice(String barCode) {
        return true;
    }

    private void refreshTabellaWO() {
        Iterator<WOTable> it = tblWoData.iterator();
        Map<String, Parte> lista = webQueryService.getParti();
        while (it.hasNext()) {
            WOTable s = it.next();
            s.setCheck(lista.get(s.getArticolo()).getVerificato());
        }
        woTblPiantaggio.refresh();
    }

    private void addTabellaWO(String code) {

        tblWoData.add(new WOTable(code, "codiceDett da barcode", true));

        woTblPiantaggio.refresh();
    }


    public void showMesage(String msg) {
        Platform.runLater(() -> {
            msgBOX.setText(msg);
        });
        plcService.cleanUpDB();
    }

    private boolean checkUDMCode(String barCode) {
        return (barCode.matches("\\d{4}(?i)(99|CS|EM|MM|MV|NQ|PI|PR|UC|UE|US)\\d{5,8}"));

    }

    public void disableWORequest(ActionEvent event) {
        webQueryService.sendDisabilitaUDM();
    }

    public void enableWORequest(ActionEvent event) {
        webQueryService.sendAbilitaUDM();
    }


    public void onCaricaParametri(ActionEvent event) {
        watchDog.resetSchedule();
        if (getPopUpPassword() == loggedUser.getPassword())
            myController.setScreen(MainStecamPiantaggioBoccoleSP1802.propertiesID);
    }


    public StatusManager getStatusManager() {
        return statusManager;
    }

    public DbService getDBService() {
        return dbService;
    }

    public void setLoggedUser(String matricola, String nomeOperatore, boolean isConduttoreDiLinea, boolean isOnLine) {
        watchDog.scheduleTimer();
        this.matricola = matricola;
        this.nomeOperatore = nomeOperatore;
        this.isConduttoreDiLinea = isConduttoreDiLinea;

        loggedUser.setLoggedIN(true);
        loggedUser.setConduttoreDiLinea(isConduttoreDiLinea);
        loggedUser.setMatricola(matricola);
        loggedUser.setNomeOperatore(nomeOperatore);

        Platform.runLater(() -> {
            lblUtenteLoggato.setText(loggedUser.getMatricola() + " - " + loggedUser.getNomeoperatore() + (loggedUser.isConduttoreDiLinea() ? " [CONDUTTORE LINEA]" : ""));
        });

        setupControlliLoggedUser();
    }

    private void setupControlliLoggedUser() {

        if (loggedUser.isConduttoreDiLinea()) {
            controlloWO.setDisable(false);
            controlloUDM.setDisable(false);
            interfacciaParametri.setDisable(false);
            synckUSERS.setDisable(false);


        } else {
            controlloWO.setDisable(true);
            controlloUDM.setDisable(true);
            interfacciaParametri.setDisable(true);
            synckUSERS.setDisable(true);
        }

    }

    public void onControlloWO(ActionEvent event) {
        isWOListPartEnabled = controlloWO.isSelected();
        webQueryService.checkSendUDM(isWOListPartEnabled, isUDMVerificaEnabled);
    }

    public void onControlloUDM(ActionEvent event) {
        isUDMVerificaEnabled = controlloUDM.isSelected();
        webQueryService.checkSendUDM(isWOListPartEnabled, isUDMVerificaEnabled);
    }

    public void onSynckUsers(ActionEvent actionEvent) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "SINCRONIZZO CON IL SERVER  ?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {
            dbService.synckUSERS();
        }
    }

    public void onLoginBtn(ActionEvent actionEvent) {
        resetLoggedUser();

    }

    public void resetLoggedUser() {
        if (loggedUser.isLoggedIN()) {
            this.matricola = "";
            this.nomeOperatore = "";
            this.isConduttoreDiLinea = false;

            loggedUser.setLoggedIN(false);
            loggedUser.setConduttoreDiLinea(false);
            loggedUser.setMatricola("");
            loggedUser.setNomeOperatore("");

            Platform.runLater(() -> {
                lblUtenteLoggato.setText(loggedUser.getMatricola() + " - " + loggedUser.getNomeoperatore() + (loggedUser.isConduttoreDiLinea() ? " [CONDUTTORE LINEA]" : ""));
            });
            watchDog.stopSchedule();
            myController.setScreen(MainStecamPiantaggioBoccoleSP1802.loginID);
        }
    }

    public void checkControlForConduttoreDiLinea(boolean isConduttoreDiLinea) {

    }

    public void onNewWO(ActionEvent actionEvent) {
        resettaStatoGlobale();
    }

    private void resettaStatoGlobale() {
        codiceRICETTA.setText("");
        codiceRICETTA.setStyle("-fx-background-color: green");
        cicloWO.setText("");
        cicloPRG.setText("");
        cicloDESCRIZIONE.setText("");

        webQueryService.cleanWO();
        tblWoData.removeAll();

        woTblPiantaggio.refresh();


        statusManager.setGlobalStatus(StatusManager.GlobalStatus.WAITING_WO);
        plcService.unsetPianta();
    }

    public void onRicetteBtn(ActionEvent actionEvent) {
        RicetteController ric = (RicetteController) myController.getController(MainStecamPiantaggioBoccoleSP1802.ricetteID);
        ric.loadDataFromDB();
        myController.setScreen(MainStecamPiantaggioBoccoleSP1802.ricetteID);
    }


    private String getPopUpPassword() {

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/popUpLogin.fxml"));
        // initializing the controller
        LoginPopUpController popupController = new LoginPopUpController();

        Parent layout;
        try {
            layout = loader.load();
            Scene scene = new Scene(layout);
            // this is the popup stage
            Stage popupStage = new Stage();
            // Giving the popup controller access to the popup stage (to allow the controller to close the stage)
            popupController.setStage(popupStage);
            if (this.main != null) {
                popupStage.initOwner(main.getPrimaryStage());
            }
            popupStage.initModality(Modality.WINDOW_MODAL);
            popupStage.setScene(scene);
            popupStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return popupController.getPassword();
    }

    public void onBarcodeTyped(ActionEvent event) {
        onNewBarCode(barcodeWO.getText());
    }

    public void onRicettaTyped(ActionEvent event) {
        onNewBarCode(codiceRICETTA.getText());
    }
}