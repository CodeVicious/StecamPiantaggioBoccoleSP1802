package stecamSP1802.services;

import java.util.concurrent.ExecutorService;

import com.google.common.base.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


//Generatore pool di thread di mapping del PLC
public class PlcService {
    final Logger Logger = LogManager.getLogger(PlcService.class);
    final PLC plcMASTER;
    final ExecutorService service;

    public PlcService(String name, String ip, byte[] plcToPc, byte[] pcToPlc, int plcToPcDb, int pcToPlcDb, double[] booleans,
                      final PLCListener listener, StatusManager statusManager, final ExecutorService service){

        //never start without Executor nor listeners
        Preconditions.checkNotNull(listener);
        Preconditions.checkNotNull(service);
        plcMASTER = new PLC(name,ip,plcToPc,pcToPlc,plcToPcDb,pcToPlcDb,booleans);


        plcMASTER.listeners.add(listener); //inserisco un observer per i cambiamenti di stato
        this.service = service;
        this.service.submit(new Thread(plcMASTER)); //Inserisco l'osservatore nel pool di thread
    }

    public void shutDownPlcService(){
        plcMASTER.liveBitEnabled = false;
        service.shutdown();
    }

    public boolean getPLCConnected(){
        return plcMASTER.connected;
    }

    public void send(String text) {
    }

    public void closeConnection() {

        this.service.shutdownNow();
    }

    public void sendCodiceRicetta(String s) {
        Logger.info("INVIO AL PLC: "+s);
        //plcMASTER.put
    }
}
