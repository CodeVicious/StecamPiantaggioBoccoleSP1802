package stecamSP1802.services.barcode;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import stecamSP1802.ConfigurationManager;
import stecamSP1802.MainController;

//Serial Service SINGLETON communication

public class SerialService {
    private final MainController mainController;
    final SerialPort comPort;

    public SerialService(final MainController mainController) {
        this.mainController = mainController;
        comPort = SerialPort.getCommPort(ConfigurationManager.getInstance().getCOMPort());
        comPort.openPort();
        comPort.addDataListener(new BarCodeListener(comPort,mainController));
    }

}
