package sample;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainController implements Initializable {
    @FXML
    Label barCode;
    @FXML
    Label plcSignal;
    @FXML
    TextField textToPLC;
    @FXML
    Button sendToPLC;


    ExecutorService executors;
    PlcService plcService;
    PLCListener listener;

    ConfigurationManager conf = ConfigurationManager.getInstance();

    public void initialize(URL location, ResourceBundle resources) {

        executors =  Executors.newCachedThreadPool();
        listener = new PLCListenerImp(this);

        plcService =  new PlcService(
                conf.getPlcName(),
                conf.getPlcIP(),
                new byte[conf.getByteArrayPcPlc()],
                new byte[conf.getByteArrayPlcPc()],
                conf.getDbNumberPcPlc(),
                conf.getDbNumberPlcPc(),
                new double[]{3.0},
                listener,
                executors
        );

    }

    public void firePLCBitChange(final int address,final int pos,final boolean val, final String plcName){
        Platform.runLater(new Runnable() {
            public void run() {
                plcSignal.setText(plcName+" Indirizzo "+ address+" Posizione "+pos+ "valore "+val);

            }
        });
    }




    public void CloseApp(ActionEvent event){
        Platform.exit();
        System.exit(0);
    }
}
