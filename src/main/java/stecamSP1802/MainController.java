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

    //Servizi
    ExecutorService executors;
    final ConfigurationManager conf = ConfigurationManager.getInstance();
    final StatusManager statusManager = StatusManager.getInstance();
    final DbService dbService = DbService.getInstance();
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
                executors
        );

        launchTime();

    }

    private void launchTime() {
        /*
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
        */

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
    }

    public void plcConnected() {
    }

    public void gDbDisconnected() {
    }

    public void gDbConnected() {
    }

    public void lDbDisconnected() {
    }

    public void lDbConnected() {
    }

    public void onRicettaOK() {
    }

    public void onRicettaKO() {
    }

    public void piantaggioBUONO() {
    }

    public void piantaggioSCARTO() {
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

                }
                webQueryService.VerificaListaPartiWO(barCode);
                plcService.sendCodiceRicetta("12345678A");
            }
        });
    }



    private boolean checkCodiceArticolo(String code){
        return(code.matches("\\d{8}[A-Z]?"));
    }
    private boolean checkUDMCode(String barCode){
        return(barCode.matches("\\d{4}(?i)(99|CS|EM|MM|MV|NQ|PI|PR|UC|UE|US)\\d{5,8}"));

    }
}