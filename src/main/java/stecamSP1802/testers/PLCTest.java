package stecamSP1802.testers;

import Moka7.*;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import stecamSP1802.ConfigurationManager;
import stecamSP1802.services.PLC;
import stecamSP1802.services.PLCListener;
import stecamSP1802.services.barcode.BarCodeListener;
import stecamSP1802.services.barcode.SerialService;

public class PLCTest {
    private S7Client moka;
    String PLCIp = "192.168.10.1";
    private int rack = 0;
    private int slot = 1;

    public PLCTest(){
        moka= new S7Client();
        int error = moka.ConnectTo(this.PLCIp, this.rack, this.slot);

        System.out.println("ERRORE "+error);
    }

    public static void main(String[] args) throws Exception {
/*
        byte[] plcToPc = new byte[4];

        byte[] pcToPlc = new byte[4];
        int plcToPcDb = 112;
        int pcToPlcDb = 114;
        double[] booleans = {3.0,0.1};

        PLC plcMASTER = new PLC("PLC GROSSO", "192.168.10.1", plcToPc, pcToPlc, plcToPcDb, pcToPlcDb, booleans);

        class imple implements PLCListener {

            public void onPLCBitChanged(int address, int pos, boolean val, String plcName) {
                switch (address) {
                    case 3:
                        switch (pos){
                            case 0:
                                System.out.println("Bit at address 0.1 of PLC "+plcName+" changed to "+val);
                        }
                }
            }
        }

        plcMASTER.listeners.add(new imple());

        Thread t1 = new Thread(plcMASTER);
        t1.start();

        Thread.sleep(2000);

        plcMASTER.putDInt(false,0,23);
*/

        SerialPort comPort;
        System.out.println("PORT "+SerialPort.getCommPorts());
        comPort = SerialPort.getCommPort("COM3");
        comPort.openPort();
        String barCode = "123456";
        System.out.println(barCode.matches("\\d{7,8}"));

        //matches("^\\d{8}[A-Z]?$"));
        //matches("^\\d{4}(?i)(99|CS|EM|MM|MV|NQ|PI|PR|UC|UE|US)\\d{5,8}$"));



        comPort.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                 return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
            }

            @Override
            public void serialEvent(SerialPortEvent serialPortEvent) {

                if (serialPortEvent.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
                       return;
                byte[] newData = new byte[comPort.bytesAvailable()];
                int numRead = comPort.readBytes(newData, newData.length);
                String str = new String(newData);
                System.out.println("CICICI "+str);
                System.out.println("Read " + numRead + " bytes."+newData);

                for (int i = 0; i < newData.length; ++i)
                    System.out.print((char)newData[i]);
                System.out.println("\n");

            }
        });

        while(true){


        }

    }

}