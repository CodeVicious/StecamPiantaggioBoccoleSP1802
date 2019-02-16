package sample;

import Moka7.*;

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
 /*       PLCTest test = new PLCTest();
        S7CpuInfo cpuInfo = new S7CpuInfo();
        System.out.println(test.moka.Connected);
        System.out.println(test.moka.GetCpuInfo(cpuInfo));

        System.out.println(cpuInfo.ASName());
        System.out.println(cpuInfo.ModuleName());


        IntByRef status = new IntByRef();
        System.out.println(test.moka.GetPlcStatus(status));

        byte[] buffer = new byte[4];
        IntByRef dime = new IntByRef();
        test.moka.DBGet(112,buffer,dime);

        S7BlockInfo blockInfo = new S7BlockInfo();
        test.moka.GetAgBlockInfo(41,112,blockInfo);

        test.moka.ReadArea(S7.S7AreaDB,112,0,4,buffer);
*/

        byte[] plcToPc = new byte[4];

        byte[] pcToPlc = new byte[4];
        int plcToPcDb = 112;
        int pcToPlcDb = 114;
        double[] booleans = {3.0,0.1};

        PLC plcMASTER = new PLC("PLC GROSSO", "192.168.10.1", plcToPc, pcToPlc, plcToPcDb, pcToPlcDb, booleans);

        class imple implements PLCListener{

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



        while(true){


        }

    }

}