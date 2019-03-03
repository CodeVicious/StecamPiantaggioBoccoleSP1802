package stecamSP1802.services.barcode;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import stecamSP1802.controllers.MainController;
import stecamSP1802.services.StatusManager;


public class BarCodeListener implements SerialPortDataListener {
    private final SerialPort comPort;
    private final MainController mainController;
    private final StatusManager statusManager;
    private Logger Logger = LogManager.getLogger(BarCodeListener.class);

    public BarCodeListener(final SerialPort comPort, final MainController mainController, StatusManager statusManager) {
        this.comPort = comPort;
        this.mainController = mainController;
        this.statusManager = statusManager;
    }

    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
    }

    public void serialEvent(SerialPortEvent serialPortEvent) {

        //Check if Status not running for BarCode acquisiztion
        if ((statusManager.getGlobalStatus() != StatusManager.GlobalStatus.WAITING_UDM) &&
                (statusManager.getGlobalStatus() != StatusManager.GlobalStatus.WAITING_WO) &&
                (statusManager.getGlobalStatus() != StatusManager.GlobalStatus.WORKING)) {
            Logger.warn("STATUS NOT READY FOR BARCODE. DISCARDED. " + statusManager.getGlobalStatus());
            return;
        }

        if (serialPortEvent.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
            Logger.warn("BARCODE EVENT DISCARDED: " + SerialPort.LISTENING_EVENT_DATA_AVAILABLE);
            return;
        }

        byte[] newData = new byte[comPort.bytesAvailable()];
        int numRead = comPort.readBytes(newData, newData.length);
        String barCode = (new String(newData)).trim();
        Logger.info("BARCODE: " + barCode + " num Bytes: " + newData.length);
        mainController.onNewBarCode(barCode);
    }
}
