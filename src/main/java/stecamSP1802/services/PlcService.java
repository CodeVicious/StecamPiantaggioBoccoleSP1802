package stecamSP1802.services;

import java.util.concurrent.ExecutorService;

import com.google.common.base.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import stecamSP1802.controllers.ForzePiantaggio;


//Generatore pool di thread di mapping del PLC
public class PlcService {
    final Logger Logger = LogManager.getLogger(PlcService.class);
    final PLC plcMASTER;
    final ExecutorService service;

    private final int pcToPlcDb;
    private final int plcToPcDb;

    public PlcService(
            String name,
            String ip, byte[] plcToPc, byte[] pcToPlc, int plcToPcDb, int pcToPlcDb, double[] booleans,
            final PLCListener listener, StatusManager statusManager, final ExecutorService service) {

        //never start without Executor nor listeners
        Preconditions.checkNotNull(listener);
        Preconditions.checkNotNull(service);

        this.plcToPcDb = plcToPc.length;
        this.pcToPlcDb = pcToPlc.length;


        plcMASTER = new PLC(
                name,
                ip,
                plcToPc,
                pcToPlc,
                plcToPcDb,
                pcToPlcDb,
                booleans,
                statusManager);

        plcMASTER.listeners.add(listener); //inserisco un observer per i cambiamenti di stato
        plcMASTER.liveBitEnabled = true;


        plcMASTER.liveBitAddress = 0;
        plcMASTER.liveBitPosition = 0;
        plcMASTER.liveBitPCDuration = 250;
        plcMASTER.liveBitPLCDuration = 500;

        this.service = service;
    }

    public void connect() {
        this.service.submit(new Thread(plcMASTER)); //Inserisco l'osservatore nel pool di thread.
    }

    public void cleanUpDB() {
        byte val = ' ';
        plcMASTER.putInt(false, 0, (short) 0);
        plcMASTER.putInt(false, 1, (short) 0);

        for (int i = 2; i < pcToPlcDb; i++)
            plcMASTER.putIntToByte(false, i, val);
        for (int i = 0; i < plcToPcDb; i++)
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
        unsetPianta();
        this.service.shutdownNow();
    }

    public void sendCodiceRicetta(String s) {
        Logger.info("INVIO AL PLC: " + s);
        byte[] toPLC = s.getBytes();

        for (int i = 0; i < s.length(); i++)
            plcMASTER.putIntToByte(false, i + 2, toPLC[i]);

        //Also il bit 0.6 di Ricetta pronta ad essere caricata
        Logger.info("SETTO OK RICETTA PRONTA 0.6");
        plcMASTER.putBool(false, 0, 6, true);
    }


    public void unsetRicettaCaricata() {
        Logger.info("RESETTO CARICA RICETTA 0.6");
        plcMASTER.putBool(false, 0, 6, false);
    }

    public void unsetPianta() {
        Logger.info("RESETTO OK PIANTA 0.5");
        plcMASTER.putBool(false, 0, 5, false);
        cleanUpDB();
    }

    public void iniziaCicloMacchina() {
        Logger.info("SETTO OK PIANTA 0.5");
        Logger.info("RESETTO RESET CICLO 0.4");
        plcMASTER.putBool(false, 0, 4, false);
        plcMASTER.putBool(false, 0, 5, true);
    }

    public void resetCiclo() {
        Logger.info("SETTO RESET CICLO 0.4");
        plcMASTER.putBool(false, 0, 4, true);
    }


    public ForzePiantaggio getForze() {

        try {
            float f1 = plcMASTER.getFloat(true, 2);
            float f2 = plcMASTER.getFloat(true, 6);
            float f3 = plcMASTER.getFloat(true, 10);
            float f4 = plcMASTER.getFloat(true, 14);
            return new ForzePiantaggio(f1, f2, f3, f4);
        } catch (Exception e) {
            Logger.error("PROBLEMA DI CONVERSIONE FORZE " + e);
        }

        return null;
    }
}
