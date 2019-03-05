package stecamSP1802.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import stecamSP1802.ConfigurationManager;
import stecamSP1802.MainStecamPiantaggioBoccoleSP1802;
import stecamSP1802.schedulers.WatchDog;
import stecamSP1802.services.StatusManagerListenerImp;
import stecamSP1802.services.WebQueryService;
import stecamSP1802.services.*;
import stecamSP1802.services.barcode.Parte;
import stecamSP1802.services.barcode.SerialService;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainController implements Initializable, ControlledScreen {
    private static Logger Logger = LogManager.getLogger(MainController.class);

    ScreensController myController;

    //Controlli interfaccia
    @FXML
    Label stecamTime;

    @FXML
    Label barcodeWO;

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
    Label codiceRICETTA;

    @FXML
    Label lblUtenteLoggato;

    @FXML
    CheckBox controlloWO;

    @FXML
    CheckBox controlloUDM;

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


    @Override
    public void setScreenParent(ScreensController screenController) {
        myController = screenController;
    }


    public void initialize(URL location, ResourceBundle resources) {

        // Setup TableView

        woTblPiantaggio.setItems(tblWoData);
        woTblPiantaggio.setEditable(false);

        tblArticolo.setCellValueFactory(new PropertyValueFactory<WOTable, String>("articolo"));
        tblDescrizione.setCellValueFactory(new PropertyValueFactory<WOTable, String>("descrizione"));
        tblCheck.setCellValueFactory(new PropertyValueFactory<WOTable, String>("check"));


        //Setup Thread Pool for PLC Service
        executors = Executors.newCachedThreadPool();

        statusManagerListener = new StatusManagerListenerImp(this);
        statusManager = new StatusManager(); // Gestore degli stati generale e di connessione
        statusManager.addListener(statusManagerListener);

        serialService = new SerialService(this, statusManager); //Gestore Bar Code
        webQueryService = new WebQueryService(statusManager);
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
            ((LoginController) myController.getController(MainStecamPiantaggioBoccoleSP1802.loginID)).onDbDISConnected();
        });
        showMesage("SQL DB SERVER DISCONNECTED ");
    }

    public void gDbConnected() {
        Logger.info("SQL DB SERVER CONNECTED ");
        Platform.runLater(() -> {
            remoteDBStatus.setText("GLOBAL DB - CONNECTED");
            remoteDBStatus.setStyle("-fx-background-color: green");
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
        showMesage("RICETTA KO! Ripetere il caricamento WorkOrder con codice UDM corretti");
    }

    public void piantaggioBUONO() {
        Platform.runLater(() -> {
            labelESITO.setText("BUONO");
            labelESITO.setStyle("-fx-background-color: green");
            statusManager.setGlobalStatus(StatusManager.GlobalStatus.WAITING_WO);
        });
        Logger.warn("PIANTAGGIO BUONO! ");
        showMesage("PIANTAGGIO BUONO! ");

        plcService.unsetPianta();
        dbService.storePiantaggio(loggedUser.getMatricola(), codiceRICETTA.getText(), barcodeWO.getText(), "OK");
    }

    public void piantaggioSCARTO() {
        Platform.runLater(() -> {
            labelESITO.setText("SCARTO");
            labelESITO.setStyle("-fx-background-color: red");
            statusManager.setGlobalStatus(StatusManager.GlobalStatus.WAITING_WO);
        });
        Logger.warn("PIANTAGGIO SCARTO! ");
        showMesage("PIANTAGGIO SCARTO! ");
        plcService.unsetPianta();
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
                            barcodeWO.setText(barCode);
                            barcodeWO.setTextFill(Color.RED);
                        } else {
                            barcodeWO.setTextFill(Color.GREEN);
                            barcodeWO.setText(barCode);
                            cicloWO.setText(barCode);
                            String ricetta = webQueryService.VerificaListaPartiWO(barCode);
                            plcService.sendCodiceRicetta(ricetta);
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
                            codiceRICETTA.setText(barCode);
                            codiceRICETTA.setTextFill(Color.RED);
                        } else {
                            codiceRICETTA.setTextFill(Color.GREEN);
                            codiceRICETTA.setText(barCode);
                        }
                        if (webQueryService.VerificaUDM(barCode))
                            refreshTabellaWO();
                        if (plcService.checkPiantaggio()) {
                            statusManager.setGlobalStatus(StatusManager.GlobalStatus.WORKING);
                            plcService.iniziaCicloMacchina();
                        }
                    }
                });
                break;

        }

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
        myController.setScreen(MainStecamPiantaggioBoccoleSP1802.propertiesID);
    }


    public StatusManager getStatusManager() {
        return statusManager;
    }

    public DbService getDBService() {
        return dbService;
    }

    public void setLoggedUser(String matricola, String nomeOperatore, boolean isConduttoreDiLinea) {
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
    }

    public void onControlloWO(ActionEvent event) {
        System.out.println(controlloWO.isSelected());
    }

    public void onControlloUDM(ActionEvent event) {
        System.out.println(controlloUDM.isSelected());
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
    }
}