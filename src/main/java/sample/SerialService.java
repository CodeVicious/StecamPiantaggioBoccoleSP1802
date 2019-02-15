package sample;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

//Serial Service SINGLETON communication

public class SerialService {

    private static SerialService ourInstance = new SerialService();
    public static SerialService getInstance() {
        return ourInstance;
    }
    final SerialPort comPort;

    private SerialService() {
        comPort = SerialPort.getCommPort(ConfigurationManager.getInstance().getCOMPort());
        comPort.openPort();
        comPort.addDataListener(new SerialPortDataListener() {

            public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_AVAILABLE; }

            public void serialEvent(SerialPortEvent event)
            {
                if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
                    return;
                byte[] newData = new byte[comPort.bytesAvailable()];
                int numRead = comPort.readBytes(newData, newData.length);
                System.out.println("LETTURA  " + numRead + " bytes.");
            }
        });
    }






}
