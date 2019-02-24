package stecamSP1802.services;

import java.util.concurrent.ExecutorService;

import com.google.common.base.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import stecamSP1802.WebQueryService;


//Generatore pool di thread di mapping del PLC
public class PlcService {
    final Logger Logger = LogManager.getLogger(PlcService.class);
    final PLC plcMASTER;
    final ExecutorService service;
    private final WebQueryService webQueryService;

    public PlcService(String name, String ip, byte[] plcToPc, byte[] pcToPlc, int plcToPcDb, int pcToPlcDb, double[] booleans,
                      final PLCListener listener, StatusManager statusManager, WebQueryService webQueryService, final ExecutorService service) {

        //never start without Executor nor listeners
        Preconditions.checkNotNull(listener);
        Preconditions.checkNotNull(service);
        Preconditions.checkNotNull(webQueryService);

        this.webQueryService = webQueryService;
        plcMASTER = new PLC(name, ip, plcToPc, pcToPlc, plcToPcDb, pcToPlcDb, booleans, statusManager);

        plcMASTER.listeners.add(listener); //inserisco un observer per i cambiamenti di stato
        this.service = service;
        this.service.execute(new Thread(plcMASTER)); //Inserisco l'osservatore nel pool di thread
        //cleanUpDB(pcToPlc,plcToPc);
    }

    public void cleanUpDB(byte[] pcToPlc, byte[] plcToPc) {
        byte val = '0';
        for (int i = 0; i < pcToPlc.length; i++)
            plcMASTER.putIntToByte(false, i, val);
        for (int i = 0; i < plcToPc.length; i++)
            plcMASTER.putIntToByte(true, i, val);
    }

    public void shutDownPlcService() {
        plcMASTER.liveBitEnabled = false;
        service.shutdown();
    }

    public boolean isPLCConnected() {
        return plcMASTER.connected;
    }


    public void closeConnection() {
        this.service.shutdownNow();
    }

    public void sendCodiceRicetta(String s) {
        Logger.info("INVIO AL PLC: " + s);
        byte[] toPLC = s.getBytes();

        for (int i = 0; i < s.length(); i++)
            plcMASTER.putIntToByte(false, i + 2, toPLC[i]);

        plcMASTER.putBool(false, 0, 6, true);
    }

    public void checkPiantaggio() {
        if (webQueryService.checkValidazioneUDM()) {
            //plcMASTER Vai
        }

    }

    public void unsetRicettaok() {
        plcMASTER.putBool(false, 0, 6, false);
    }

}
