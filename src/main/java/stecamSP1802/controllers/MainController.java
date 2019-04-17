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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import stecamSP1802.ConfigurationManager;
import stecamSP1802.MainStecamPiantaggioBoccoleSP1802;
import stecamSP1802.helper.SwitchGUIJna;
import stecamSP1802.schedulers.WatchDog;
import stecamSP1802.services.StatusManagerListenerImp;
import stecamSP1802.services.WebQueryService;
import stecamSP1802.services.*;
import stecamSP1802.services.barcode.Parte;
import stecamSP1802.services.barcode.SerialService;
import stecamSP1802.services.barcode.WorkOrder;

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
    Button btnRicette;

    @FXML
    Label lblUtenteLoggato;

    @FXML
    Label errorBar;


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
    private TableColumn tblUdM;

    @FXML
    private TableColumn tblArticolo;

    @FXML
    private TableColumn tblDescrizione;

    @FXML
    private TableColumn tblCheck;

    @FXML
    private TextField lastUdM;

    @FXML
    private TextField lastCodProdotto;

    @FXML
    private ImageView imageALERTS;


    final ObservableList<WOTable> tblWoData = FXCollections.observableArrayList(WOTable.extractor());

    //Servizi
    ExecutorService executors;
    final ConfigurationManager conf = ConfigurationManager.getInstance();
    final LoggedUser loggedUser = LoggedUser.getInstance();

    StatusManager statusManager;
    DbService dbService;
    SerialService serialService;
    PlcService plcService;
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

        tblUdM.setCellValueFactory(new PropertyValueFactory<WOTable, String>("uDm"));
        tblArticolo.setCellValueFactory(new PropertyValueFactory<WOTable, String>("articolo"));
        tblDescrizione.setCellValueFactory(new PropertyValueFactory<WOTable, String>("descrizioneDett"));
        tblCheck.setCellValueFactory(new PropertyValueFactory<WOTable, String>("check"));

        tblCheck.setCellFactory(e -> new TableCell<ObservableList<String>, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(item);
                    if (item.matches("OK"))
                        this.setStyle("-fx-background-color: green;");
                    else
                        this.setStyle("-fx-background-color: red;");

                }
            }
        });


        //Setup Thread Pool for PLC Service
        executors = Executors.newCachedThreadPool();

        statusManagerListener = new StatusManagerListenerImp(this, plcService);
        statusManager = new StatusManager(); // Gestore degli stati generale e di connessione
        statusManager.addListener(statusManagerListener);
        WebQueryService.getInstance().setStatusManager(statusManager);
        WebQueryService.getInstance().setMainController(this);

        serialService = new SerialService(this, statusManager); //Gestore Bar Code
        dbService = new DbService(statusManager); //Gestore interfacce DB
        dbService.connectLocalDB();
        conf.setDbService(dbService);
        conf.getDBConfiguration();

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
                executors
        ); // Gestore interfaccia PLC

        launchTime(); //Clock TODO:REMOVE?
    }

    public void startMainServices() {
        dbService.connectRemoteDB();
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
        dbService.saveWO();
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
        plcService.cleanUpDB();
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
            WorkOrder wo = WorkOrder.getInstance();
            codiceRICETTA.setText(wo.getCodiceRicetta());
            codiceRICETTA.setStyle("-fx-background-color: green");
            cicloWO.setText(wo.getBarCodeWO());
            cicloPRG.setText(wo.getCodiceRicetta());
            cicloDESCRIZIONE.setText(wo.getDescrizione());

            Map<String, Parte> lista = wo.getListaParti();
            for (String art : lista.keySet()) {
                tblWoData.add(new WOTable(lista.get(art).getCodiceUdM(), lista.get(art).getCodice(), lista.get(art).getDescrizione(), lista.get(art).getVerificato()));
            }

            statusManager.setGlobalStatus(StatusManager.GlobalStatus.WAITING_UDM);
        });

        showMesage("RICETTA CARICATA! ");
    }

    public void onRicettaKO() {
        plcService.unsetRicettaCaricata();
        Logger.warn("RICETTA KO! ");
        Platform.runLater(() -> {
            barcodeWO.setText("");
            codiceRICETTA.setText(" NON PRESENTE ");
            codiceRICETTA.setStyle("-fx-background-color: red");
            showError("Codice Ricetta " + WorkOrder.getInstance().getCodiceRicetta() + " rigettato -> Nuovo WO");
            statusManager.setGlobalStatus(StatusManager.GlobalStatus.WAITING_WO);
        });
        showMesage("RICETTA KO! Ripetere il caricamento WorkOrder con codiceDett UDM corretti");
    }

    public void piantaggioBUONO() {
        watchDog.resetSchedule();
        Platform.runLater(() -> {
            labelESITO.setText("BUONO");
            labelESITO.setStyle("-fx-background-color: green");
        });
        Logger.warn("PIANTAGGIO BUONO! ");
        showMesage("PIANTAGGIO BUONO! ");

        dbService.storePiantaggio(loggedUser.getMatricola(), WorkOrder.getInstance().getCodiceRicetta(),
                WorkOrder.getInstance().getBarCodeWO(), "OK");
    }

    public void piantaggioSCARTO() {
        watchDog.resetSchedule();
        Platform.runLater(() -> {
            labelESITO.setText("SCARTO");
            labelESITO.setStyle("-fx-background-color: red");

        });
        Logger.warn("PIANTAGGIO SCARTO! ");
        showMesage("PIANTAGGIO SCARTO! ");

        dbService.storePiantaggio(loggedUser.getMatricola(), WorkOrder.getInstance().getCodiceRicetta(),
                WorkOrder.getInstance().getBarCodeWO(), "KO");
    }

    public void onNewBarCode(String barCode) {

        switch (statusManager.getGlobalStatus()) {

            case WAITING_WO:
                Platform.runLater(new Runnable() {
                    public void run() {
                        //check BarCode
                        if (!barCode.matches("\\d{7,8}")) {
                            Logger.error("Il BarCode " + barCode + " NON E' UN VALIDO WORK ORDER");
                            showError("Il BarCode " + barCode + " NON E' UN VALIDO WORK ORDER");
                            barcodeWO.setText(barCode);
                            barcodeWO.setStyle("-fx-control-inner-background: red");
                        } else {
                            barcodeWO.setText(barCode);
                            barcodeWO.setStyle("-fx-control-inner-background: green");
                            showError("");
                            cicloWO.setText(barCode);

                            if (isWOListPartEnabled) {
                                EsitoWebQuery esito = WebQueryService.getInstance().VerificaListaPartiWO(barCode); //Verifica il WO e carica la lista parti dal server

                                if (esito.getEsitoQuery() == EsitoWebQuery.ESITO.OK) {
                                    codiceRICETTA.setText(esito.getResultQuery());
                                    showMesage("RICETTA " + esito.getResultQuery() + " INVIATA ALLA MACCHINA. IN ATTESA DI ACK/NACK");
                                    statusManager.setGlobalStatus(StatusManager.GlobalStatus.WAITING_RICETTA_OK_KO);
                                    plcService.sendCodiceRicetta(esito.getResultQuery());
                                } else {
                                    Logger.warn("Query WO " + barCode + " RIGETTATA DAL SERVER");
                                    barcodeWO.setStyle("-fx-control-inner-background: red");
                                    showMesage("WO " + barCode + " KO! " + esito.getResultQuery());
                                    showError("WO " + barCode + " KO! " + esito.getResultQuery());
                                }
                            } else {
                                WorkOrder.getInstance().setBarCodeWO(barCode);
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
                            showError("Il BarCode " + barCode + " NON E' UN VALIDO CODICE ARTICOLO");
                            showMesage("CODICE ARTICOLO - ERRATO");
                            codiceRICETTA.setText(barCode);
                            codiceRICETTA.setStyle("-fx-control-inner-background: red");

                        } else {
                            codiceRICETTA.setText(barCode);
                            codiceRICETTA.setStyle("-fx-control-inner-background: green");
                            codiceRICETTA.setText("checking"); //Chiedo il caricamento della ricetta direttamente al PLC
                            showError("");
                            WorkOrder.getInstance().setCodiceRicetta(barCode);
                            if (dbService.loadRicetta(barCode)) {
                                showMesage("RICETTA " + barCode + " INVIATA ALLA MACCHINA. IN ATTESA DI ACK/NACK");
                                statusManager.setGlobalStatus(StatusManager.GlobalStatus.WAITING_RICETTA_OK_KO);
                                plcService.sendCodiceRicetta(barCode);
                            } else {
                                Logger.warn("RCETTA " + barCode + " NON PRESENTE NEL DB!");
                                codiceRICETTA.setStyle("-fx-control-inner-background: red");
                                onRicettaKO();
                            }
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
                            showError("Il BarCode " + barCode + " NON E' UN VALIDO UDM CODE");
                            showMesage("CODICE UdM - ERRATO");

                            lastUdM.setText(barCode);
                            lastUdM.setStyle("-fx-control-inner-background: red");

                        } else {
                            lastUdM.setText(barCode);
                            lastUdM.setStyle("-fx-control-inner-background: green");
                            showError("");

                            if (isUDMVerificaEnabled) {
                                EsitoWebQuery esito = WebQueryService.getInstance().VerificaUDM(barCode);
                                if (esito.getEsitoQuery() == EsitoWebQuery.ESITO.OK) {
                                    if (VerificaCodice(esito.getResultQuery())) //Controllo se nella lista componenti e setto il check
                                    {
                                        showMesage("CODICE "+esito.getResultQuery()+" ACCETTATO");
                                        lastCodProdotto.setText(esito.getResultQuery());
                                        refreshTabellaWO(barCode, esito.getResultQuery());

                                    } else {
                                        Logger.warn("Il BarCode " + barCode + " UdM NON E' NELLA LISTA COMPONENTI");
                                        lastUdM.setStyle("-fx-control-inner-background: red");
                                        showMesage("UdM " + barCode + " CODICE " + esito.getResultQuery() + " NON E' UN COMPONENTE CORRETTO");
                                    }

                                    if (WorkOrder.getInstance().checkLavorabile()) {
                                        statusManager.setGlobalStatus(StatusManager.GlobalStatus.WORKING);
                                        plcService.iniziaCicloMacchina();
                                    }

                                } else {
                                    Logger.warn("Il BarCode " + barCode + " UdM RIGETTATO DAL SERVER");
                                    lastUdM.setStyle("-fx-control-inner-background: red");
                                    showMesage("UdM " + barCode + " RIGETTATO DA SERVER " + esito.getResultQuery());
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
                            showError("Il BarCode " + barCode + " NON E' UN VALIDO CODICE COMPONENTE");
                            showMesage("CODICE COMPONENTE- ERRATO");

                            lastCodProdotto.setText(barCode);
                            lastCodProdotto.setStyle("-fx-control-inner-background: red");

                        } else {
                            lastCodProdotto.setText(barCode);
                            lastCodProdotto.setStyle("-fx-control-inner-background: green");
                            showError("");

                            if (VerificaCodice(barCode)) {
                                refreshTabellaWO(lastUdM.getText(), barCode); //Aggiungo l'UdM

                                if (WorkOrder.getInstance().checkLavorabile()) {
                                    statusManager.setGlobalStatus(StatusManager.GlobalStatus.WORKING);
                                    plcService.iniziaCicloMacchina();
                                } else {
                                    showMesage("Inserire il Codice UdM");
                                    statusManager.setGlobalStatus(StatusManager.GlobalStatus.WAITING_UDM);
                                }
                            } else {
                                Logger.error("Il BarCode " + barCode + " NON E' NELLA LISTA COMPONENTI");
                                lastCodProdotto.setStyle("-fx-control-inner-background: red");
                                showMesage("COMPONENTE NON PRESENTE NELLA LISTA");
                            }
                        }
                    }
                });
                break;
            default:
                Logger.warn("BARCODE " + barCode + " SCARTATO");

        }

    }


    private boolean VerificaCodice(String codice) {
        Logger.info("VERIFICA CODICE "+codice);
        if (WorkOrder.getInstance().getListaParti().containsKey(codice.trim())) {
            Logger.info("CODICE OK "+codice);
            Parte parte = WorkOrder.getInstance().getListaParti().get(codice);
            parte.setVerificato(true);
            Logger.info("UDM OK - CODE " + codice);
            return true;
        }
        return false;
    }


    private void refreshTabellaWO(String UdM, String codProdotto) {
        Map<String, Parte> lista = WorkOrder.getInstance().getListaParti();
        lista.get(codProdotto).setCodiceUdM(UdM);

        Iterator<WOTable> it = tblWoData.iterator();

        while (it.hasNext()) {
            WOTable s = it.next();
            s.setCheck(lista.get(s.getArticolo()).getVerificato());
            s.setuDm(lista.get(s.getArticolo()).getCodiceUdM());
        }
    }


    public void showMesage(String msg) {
        Platform.runLater(() -> {
            msgBOX.setText(msg);
        });
    }


    public void showError(String msg) {
        Platform.runLater(() -> {
            errorBar.setText(msg);
        });
    }


    public void disableWORequest(ActionEvent event) {
        WebQueryService.getInstance().sendDisabilitaUDM();
    }

    public void enableWORequest(ActionEvent event) {
        WebQueryService.getInstance().sendAbilitaUDM();
    }


    public void onCaricaParametri(ActionEvent event) {

        String res = getPopUpPassword();
        if (!res.matches("CANCEL")) {
            if (res.matches(loggedUser.getPassword()))
                myController.setScreen(MainStecamPiantaggioBoccoleSP1802.propertiesID);
            else {
                Alert alert = new Alert(Alert.AlertType.ERROR,
                        "PASSWORD SBAGLIATA", ButtonType.OK);
                alert.showAndWait();
            }
        }
    }


    public StatusManager getStatusManager() {
        return statusManager;
    }

    public DbService getDBService() {
        return dbService;
    }

    public void setLoggedUser(String matricola, String password, String nomeOperatore, boolean isConduttoreDiLinea, boolean isOnLine) {
        watchDog.scheduleTimer();

        this.matricola = matricola;
        this.nomeOperatore = nomeOperatore;
        this.isConduttoreDiLinea = isConduttoreDiLinea;

        loggedUser.setLoggedIN(true);
        loggedUser.setConduttoreDiLinea(isConduttoreDiLinea);
        loggedUser.setMatricola(matricola);
        loggedUser.setPassword(password);
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
            btnRicette.setDisable(false);

            synckUSERS.setDisable(false);


        } else {
            controlloWO.setDisable(true);
            controlloUDM.setDisable(true);

            btnRicette.setDisable(true);

            interfacciaParametri.setDisable(true);
            synckUSERS.setDisable(true);
        }

        setUpControls(statusManager.getGlobalStatus());


    }

    public void onControlloWO(ActionEvent event) {
        isWOListPartEnabled = controlloWO.isSelected();
        WebQueryService.getInstance().checkSendUDM(isWOListPartEnabled, isUDMVerificaEnabled);
    }

    public void onControlloUDM(ActionEvent event) {
        isUDMVerificaEnabled = controlloUDM.isSelected();
        WebQueryService.getInstance().checkSendUDM(isWOListPartEnabled, isUDMVerificaEnabled);
    }

    public void onSynckUsers(ActionEvent actionEvent) {

        String res = getPopUpPassword();
        if (!res.matches("CANCEL")) {
            if (res.matches(loggedUser.getPassword())) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                        "SINCRONIZZO CON IL SERVER  ?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
                alert.showAndWait();
                if (alert.getResult() == ButtonType.YES) {
                    dbService.synckUSERS();
                    showError("SINCRONIZZAZIONE CONCLUSA");
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR,
                        "PASSWORD SBAGLIATA", ButtonType.OK);
                alert.showAndWait();
            }
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


    public void onNewWO(ActionEvent actionEvent) {
        resettaStatoGlobale();
    }

    private void resettaStatoGlobale() {
        barcodeWO.setText("");
        codiceRICETTA.setText("");
        lastUdM.setText("");
        lastCodProdotto.setText("");

        barcodeWO.setStyle("-fx-background-color: white");
        codiceRICETTA.setStyle("-fx-background-color: white");
        lastUdM.setStyle("-fx-background-color: white");
        lastCodProdotto.setStyle("-fx-background-color: white");

        cicloWO.setText("");
        cicloPRG.setText("");
        cicloDESCRIZIONE.setText("");
        tblWoData.clear();


        WebQueryService.getInstance().cleanWO();

        woTblPiantaggio.setItems(tblWoData);


        statusManager.setGlobalStatus(StatusManager.GlobalStatus.WAITING_WO);
        plcService.unsetPianta();
    }

    public void onRicetteBtn(ActionEvent actionEvent) {

        String res = getPopUpPassword();
        if (!res.matches("CANCEL")) {
            if (res.matches(loggedUser.getPassword())) {
                RicetteController ric = (RicetteController) myController.getController(MainStecamPiantaggioBoccoleSP1802.ricetteID);
                ric.loadDataFromDB();
                myController.setScreen(MainStecamPiantaggioBoccoleSP1802.ricetteID);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR,
                        "PASSWORD SBAGLIATA", ButtonType.OK);
                alert.showAndWait();
            }
        }

    }


    private String getPopUpPassword() {

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/popUpLogin.fxml"));
        // initializing the controller

        LoginPopUpController popupController;

        Parent layout;
        try {
            layout = loader.load();
            popupController = (LoginPopUpController) loader.getController();
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
            return popupController.getPassword();
        } catch (IOException e) {
            Logger.error(e);
            return "";
        }

    }


    public void setControlloOFFLine() {
        isWOListPartEnabled = false;
        isUDMVerificaEnabled = false;

        controlloWO.setSelected(false);
        controlloUDM.setSelected(false);

        Image offline = new Image(getClass().getResourceAsStream("/HMI/offline-icon.png"));
        imageALERTS.setImage(offline);
        LoginController lc = (LoginController) myController.getController(MainStecamPiantaggioBoccoleSP1802.loginID);

        showError("ATTENZIONE CONNESSIONE WEBQUERY ASSENTE! AUTORIZZARE IL CICLO.");

        lc.setOFFLINEControls();

        if (!loggedUser.isConduttoreDiLinea()) {

            Alert alert = new Alert(Alert.AlertType.INFORMATION,
                    "PER CONTINUARE OFF-LINE E' NECESSARIO IL LOGIN DA PARTE DI UN CONDUTTORE DI LINEA", ButtonType.OK);
            alert.showAndWait();
            resetLoggedUser();
        } else {
            String res = getPopUpPassword();
            if (!res.matches("CANCEL")) {
                if (!res.matches(loggedUser.getPassword())) {
                    resetLoggedUser();
                }
            }
        }
    }

    public void setControlloONLine() {
        imageALERTS.setImage(null);
        showError("WebQuery onLine");
    }

    public void setUpControls(StatusManager.GlobalStatus globalStatus) {

        if (!loggedUser.isConduttoreDiLinea()) {
            barcodeWO.setDisable(true);
            codiceRICETTA.setDisable(true);
            lastUdM.setDisable(true);
            lastCodProdotto.setDisable(true);
        } else {
            switch (globalStatus) {
                case WAITING_WO:
                    barcodeWO.setDisable(false);
                    codiceRICETTA.setDisable(true);
                    lastUdM.setDisable(true);
                    lastCodProdotto.setDisable(true);
                    break;
                case WAITING_RICETTA_OK_KO:
                    barcodeWO.setDisable(true);
                    codiceRICETTA.setDisable(true);
                    lastUdM.setDisable(true);
                    lastCodProdotto.setDisable(true);
                    break;
                case WAITING_CODICE_RICETTA:
                    barcodeWO.setDisable(true);
                    codiceRICETTA.setDisable(false);
                    lastUdM.setDisable(true);
                    lastCodProdotto.setDisable(true);
                    break;
                case WAITING_UDM:
                    barcodeWO.setDisable(true);
                    codiceRICETTA.setDisable(true);
                    lastUdM.setDisable(false);
                    lastCodProdotto.setDisable(true);
                    break;
                case WAITING_CODICE_COMPONENTE:
                    barcodeWO.setDisable(true);
                    codiceRICETTA.setDisable(true);
                    lastUdM.setDisable(true);
                    lastCodProdotto.setDisable(false);
                    break;
                case WORKING:
                    barcodeWO.setDisable(true);
                    codiceRICETTA.setDisable(true);
                    lastUdM.setDisable(true);
                    lastCodProdotto.setDisable(true);
                    break;
                default:
                    barcodeWO.setDisable(true);
                    codiceRICETTA.setDisable(true);
                    lastUdM.setDisable(true);
                    lastCodProdotto.setDisable(true);
            }
        }
    }

    public void onWOTyped(ActionEvent actionEvent) {
        onNewBarCode(barcodeWO.getText());
    }

    public void onUdmTyped(ActionEvent actionEvent) {
        onNewBarCode(lastUdM.getText());
    }

    public void onCodProdottoTyped(ActionEvent actionEvent) {
        onNewBarCode(lastCodProdotto.getText());
    }

    public void onRicettaTyped(ActionEvent event) {
        onNewBarCode(codiceRICETTA.getText());
    }

    public void onSwitchHMI(ActionEvent actionEvent) {
        SwitchGUIJna.SwitchGUI(ConfigurationManager.getInstance().getHmi());
    }


}