package sample;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StatusManager {
    private static Logger logger = LogManager.getLogger(StatusManager.class);

    private static StatusManager ourInstance = new StatusManager();
    synchronized public static StatusManager getInstance() {
        return ourInstance;
    } //Thread Safe Singleton
    private StatusManager() {}

    private enum PlcStatus {
        STARTUP, PLC_CONNECTING, PLC_CONNECTED
    }

    private enum LocalDbStatus{
        LOCAL_DB_CONNECTING, LOCAL_DB_CONNECTED, LOCAL_DB_DISABLED
    }

    private enum GlobalDbStatus{
        GLOBAL_DB_CONNECTING, GLOBAL_DB_CONNECTED, GLOBAL_DB_DISABLED,
    }

    private PlcStatus  plcStatus;
    private LocalDbStatus  localDbStatus;
    private GlobalDbStatus globalDbStatus;

    synchronized public PlcStatus getPlcStatus() {
        return plcStatus;
    }

    synchronized public void setPlcStatus(PlcStatus plcStatus) {
        
        this.plcStatus = plcStatus;
    }

    synchronized public LocalDbStatus getLocalDbStatus() {
        return localDbStatus;
    }

    synchronized public void setLocalDbStatus(LocalDbStatus localDbStatus) {
        this.localDbStatus = localDbStatus;
    }

    synchronized public GlobalDbStatus getGlobalDbStatus() {
        return globalDbStatus;
    }

    synchronized public void setGlobalDbStatus(GlobalDbStatus globalDbStatus) {
        this.globalDbStatus = globalDbStatus;
    }
}
