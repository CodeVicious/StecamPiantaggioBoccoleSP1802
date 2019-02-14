package sample;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainController implements Initializable {

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
                new double[]{0.1,0.2},
                listener,
                executors
        );

    }

    public void firePLCBitChange(int address, int pos, boolean val, String plcName){

    }


    public void CloseApp(ActionEvent event){
        Platform.exit();
        System.exit(0);
    }
}
