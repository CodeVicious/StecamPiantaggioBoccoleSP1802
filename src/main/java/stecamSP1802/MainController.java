package stecamSP1802;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import stecamSP1802.services.*;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Thread.sleep;

public class MainController implements Initializable {

    //Controlli interfaccia
    @FXML
    Label stecamTime;

    //Servizi
    ExecutorService executors;
    final ConfigurationManager conf = ConfigurationManager.getInstance();
    final StatusManager statusManager = StatusManager.getInstance();
    final DbService dbService = DbService.getInstance();
    PlcService plcService;

    // Observers
    PLCListener plcListener ;
    StatusManagerListener statusManagerListener;


    private long minute;
    private long second;
    private int hour;

    public void initialize(URL location, ResourceBundle resources) {

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

    public void onNewBarCode() {
        //check BarCode
        //chiedi a db ed estrai codice prodotto

        plcService.sendCodiceRicetta("12345678A");
    }
}
