package stecamSP1802.controllers;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import stecamSP1802.ConfigurationManager;
import stecamSP1802.MainStecamPiantaggioBoccoleSP1802;
import stecamSP1802.StatusManagerListenerImp;
import stecamSP1802.WebQueryService;
import stecamSP1802.services.*;
import stecamSP1802.services.barcode.SerialService;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
    Label localDBStatus;

    @FXML
    Label globalDBStatus;

    @FXML
    Label buttonESITO;

    @FXML
    Label codiceRICETTA;

    @FXML
    ToggleButton disattivaWO;

    @FXML
    ToggleButton attivaWO;

    //Servizi
    ExecutorService executors;
    final ConfigurationManager conf = ConfigurationManager.getInstance();
    StatusManager statusManager;
    DbService dbService;
    SerialService serialService;
    PlcService plcService;
    WebQueryService webQueryService;

    // Observers
    PLCListener plcListener;
    StatusManagerListener statusManagerListener;


    private long minute;
    private long second;
    private int hour;

    @Override
    public void setScreenParent(ScreensController screenController) {
        myController = screenController;
    }


    public void initialize(URL location, ResourceBundle resources) {
        executors = Executors.newCachedThreadPool();
        statusManagerListener = new StatusManagerListenerImp(this);
        serialService = new SerialService(this);
        webQueryService = new WebQueryService();
        statusManager = new StatusManager();
        statusManager.addListener(statusManagerListener);
        plcListener = new PLCListenerImp(this, statusManager);

        dbService = new DbService(statusManager);

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
        );



        launchTime();

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
        });
    }

    public void gDbDisconnected() {
        Logger.warn("SQL DB SERVER DISCONNECTED ");
        Platform.runLater(() -> {
            globalDBStatus.setText("GLOBAL DB - DISCONNECTED");
            globalDBStatus.setStyle("-fx-background-color: red");
        });
        showMesage("SQL DB SERVER DISCONNECTED ");
    }

    public void gDbConnected() {
        Logger.info("SQL DB SERVER CONNECTED ");
        Platform.runLater(() -> {
            globalDBStatus.setText("GLOBAL DB - CONNECTED");
            globalDBStatus.setStyle("-fx-background-color: green");
        });
        showMesage("SQL DB SERVER CONNECTED ");
    }

    public void lDbDisconnected() {
        Logger.warn("LOCAL SQL DB SERVER DISCONNECTED ");
        Platform.runLater(() -> {
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
        });
        showMesage("LOCAL SQL DB SERVER CONNECTED");
    }

    public void onRicettaOK() {
        plcService.unsetRicettaok(); //Abbasso il bit di carica ricetta al PLC

        Logger.warn("RICETTA CARICATA! ");
        Platform.runLater(() -> {
            codiceRICETTA.setText(webQueryService.getWO().getCodiceRicetta());
            codiceRICETTA.setStyle("-fx-background-color: green");
        });

        showMesage("RICETTA CARICATA! ");
    }

    public void onRicettaKO() {
        plcService.unsetRicettaok();
        Logger.warn("RICETTA KO! ");
        Platform.runLater(() -> {
            codiceRICETTA.setText(webQueryService.getWO().getCodiceRicetta() + "NON PRESENTE");
            codiceRICETTA.setStyle("-fx-background-color: red");
        });
        showMesage("RICETTA KO! ");
    }

    public void piantaggioBUONO() {
        Platform.runLater(() -> {
            buttonESITO.setText("BUONO");
            localDBStatus.setStyle("-fx-background-color: green");
        });
        Logger.warn("PIANTAGGIO BUONO! ");
        showMesage("PIANTAGGIO BUONO! ");

        plcService.unsetPianta();
    }

    public void piantaggioSCARTO() {
        Platform.runLater(() -> {
            buttonESITO.setText("SCARTO");
            localDBStatus.setStyle("-fx-background-color: red");
        });
        Logger.warn("PIANTAGGIO SCARTO! ");
        showMesage("PIANTAGGIO SCARTO! ");
    }

    public void onNewBarCode(String barCode) {

        Platform.runLater(new Runnable() {
            public void run() {
                //check BarCode
                if (!barCode.matches("\\d{7,8}\r")) {
                    Logger.error("Il BarCode " + barCode + " NON E' UN VALIDO WORK ORDER");
                    barcodeWO.setText(barCode);
                    barcodeWO.setTextFill(Color.RED);
                } else {
                    barcodeWO.setTextFill(Color.GREEN);
                    barcodeWO.setText(barCode);
                    String ricetta = webQueryService.VerificaListaPartiWO(barCode, statusManager);
                    plcService.sendCodiceRicetta(ricetta);
                }

            }
        });
        /*
        switch (statusManager.getGlobalStatus()) {

            case RUNNING:
                Platform.runLater(new Runnable() {
                    public void run() {
                        //check BarCode
                        if (!barCode.matches("\\d{7,8}\r")) {
                            Logger.error("Il BarCode " + barCode + " NON E' UN VALIDO WORK ORDER");
                            barcodeWO.setText(barCode);
                            barcodeWO.setTextFill(Color.RED);
                        } else {
                            barcodeWO.setTextFill(Color.GREEN);
                            barcodeWO.setText(barCode);

                        }
                        webQueryService.VerificaListaPartiWO(barCode,statusManager);
                        plcService.sendCodiceRicetta("12345678A");
                    }
                });
                break;
            case WAITING_UDM:
                Platform.runLater(new Runnable() {
                    public void run() {
                        //check BarCode
                        if (!barCode.matches("\\d{8}[A-Z]?")) {
                            Logger.error("Il BarCode " + barCode + " NON E' UN VALIDO UDM CODE");
                            barcodeWO.setText(barCode);
                            barcodeWO.setTextFill(Color.RED);
                        } else {
                            barcodeWO.setTextFill(Color.GREEN);
                            barcodeWO.setText(barCode);

                        }
                        webQueryService.VerificaUDM(barCode,statusManager);
                        plcService.checkPiantaggio();
                    }
                });
                break;

        }
        */
    }

    public void showMesage(String msg) {
        Platform.runLater(() -> {
            msgBOX.setText(msg);
        });
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
        myController.setScreen(MainStecamPiantaggioBoccoleSP1802.propertiesID);
        System.out.println("UU");
    }
}