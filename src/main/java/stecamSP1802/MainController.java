package stecamSP1802;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import stecamSP1802.services.*;
import stecamSP1802.services.barcode.SerialService;
import sun.applet.Main;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Thread.sleep;

public class MainController implements Initializable {
    private static Logger Logger = LogManager.getLogger(MainController.class);


    //Controlli interfaccia
    @FXML
    Label stecamTime;

    @FXML
    Label barcodeWO;

    @FXML
    Label msgBOX;

    //Servizi
    ExecutorService executors;
    final ConfigurationManager conf = ConfigurationManager.getInstance();
    final StatusManager statusManager = StatusManager.getInstance();
    DbService dbService;
    SerialService serialService;
    PlcService plcService;
    WebQueryService webQueryService;

    // Observers
    PLCListener plcListener ;
    StatusManagerListener statusManagerListener;


    private long minute;
    private long second;
    private int hour;


    public void initialize(URL location, ResourceBundle resources) {
        serialService = new SerialService(this);
        webQueryService =new WebQueryService();
        dbService = new DbService(statusManager);



        executors =  Executors.newCachedThreadPool();
        plcListener = new PLCListenerImp(this, statusManager);
        statusManagerListener = new StatusManagerListenerImp(this);


        plcService =  new PlcService(
                conf.getPlcName(),
                conf.getPlcIP(),
                new byte[conf.getByteArrayPcPlc()],
                new byte[conf.getByteArrayPlcPc()],
                conf.getDbNumberPcPlc(),
                conf.getDbNumberPlcPc(),
                conf.getBitMonitor(),
                plcListener ,
                statusManager,
                webQueryService,
                executors
        );

        launchTime();

    }

    private void launchTime() {

        Task task = new Task<Void>() {
            @Override public Void call() {
                for (;;) {
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



    public void firePLCBitChange(final int address,final int pos,final boolean val, final String plcName){
        Platform.runLater(new Runnable() {
            public void run() {
                //plcSignal.setText(plcName+" Indirizzo "+ address+" Posizione "+pos+ "valore "+val);

            }
        });
    }


    public void CloseApp(ActionEvent event){
        plcService.closeConnection();
        Platform.exit();
        System.exit(0);
    }

    public void plcDisconnected() {
        Logger.warn("PLC DISCONNECTED ");
        showMesage("PLC DISCONNECTED ");
    }

    public void plcConnected() {
        Logger.info("PLC CONNECTED ");
        showMesage("PLC CONNECTED ");
    }

    public void gDbDisconnected() {
        Logger.warn("SQL DB SERVER DISCONNECTED ");
        showMesage("SQL DB SERVER DISCONNECTED ");
    }

    public void gDbConnected() {
        Logger.info("SQL DB SERVER CONNECTED ");
        showMesage("SQL DB SERVER CONNECTED ");
    }

    public void lDbDisconnected() {
        Logger.warn("LOCAL SQL DB SERVER DISCONNECTED ");
        showMesage("LOCAL SQL DB SERVER DISCONNECTED ");
    }

    public void lDbConnected() {
        Logger.info("LOCAL SQL DB SERVER CONNECTED ");
        showMesage("LOCAL SQL DB SERVER CONNECTED");
    }

    public void onRicettaOK() {
        Logger.warn("RICETTA OK! ");
        showMesage("RICETTA OK! ");
    }

    public void onRicettaKO() {
        Logger.warn("RICETTA KO! ");
        showMesage("RICETTA KO! ");
    }

    public void piantaggioBUONO() {
        Logger.warn("PIANTAGGIO BUONO! ");
        showMesage("PIANTAGGIO BUONO! ");
    }

    public void piantaggioSCARTO() {
        Logger.warn("PIANTAGGIO SCARTO! ");
        showMesage("PIANTAGGIO SCARTO! ");
    }

    public void onNewBarCode(String barCode) {
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
    }

    private void showMesage(String msg){
        Platform.runLater(()->{
            msgBOX.setText(msg);
        });
    }

    private boolean checkUDMCode(String barCode){
        return(barCode.matches("\\d{4}(?i)(99|CS|EM|MM|MV|NQ|PI|PR|UC|UE|US)\\d{5,8}"));

    }
}