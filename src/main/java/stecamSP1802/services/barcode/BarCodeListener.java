package stecamSP1802.services.barcode;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import stecamSP1802.MainController;


public class BarCodeListener implements SerialPortDataListener {
    private final SerialPort comPort;
    private final MainController mainController;
    private Logger Logger = LogManager.getLogger(BarCodeListener.class);

    public BarCodeListener(final SerialPort comPort, final MainController mainController){
        this.comPort = comPort;
        this.mainController = mainController;
    }

    public int getListeningEvents() {
        return 0;
    }

    public void serialEvent(SerialPortEvent serialPortEvent) {
                if (serialPortEvent.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
                    Logger.info("BARCODE EVENT DISCARDED: " + SerialPort.LISTENING_EVENT_DATA_AVAILABLE);
                    return;
                }
                byte[] newData = new byte[comPort.bytesAvailable()];
                int numRead = comPort.readBytes(newData, newData.length);
                Logger.info("BARCODE: "+newData+" num Bytes: "+newData.length);
                mainController.onNewBarCode();
    }
}
