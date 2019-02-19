package stecamSP1802.services;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class StatusManager {
    private static Logger Logger = LogManager.getLogger(StatusManager.class);

    private static StatusManager ourInstance = new StatusManager();
    synchronized public static StatusManager getInstance() {
        return ourInstance;
    } //Thread Safe Singleton

    private List<StatusManagerListener> listeners;
    private StatusManager() {}

    public static enum GlobalStatus {
        CONNECTING, CONNECTED, RUNNING, WAITING_UDM
    }

    public static enum PlcStatus {
        STARTUP, PLC_CONNECTING, PLC_CONNECTED
    }

    public static  enum LocalDbStatus{
        LOCAL_DB_CONNECTING, LOCAL_DB_CONNECTED, LOCAL_DB_DISABLED
    }

    public static  enum GlobalDbStatus{
        GLOBAL_DB_CONNECTING, GLOBAL_DB_CONNECTED, GLOBAL_DB_DISABLED,
    }

    private PlcStatus  plcStatus;
    private LocalDbStatus  localDbStatus;
    private GlobalDbStatus globalDbStatus;
    private GlobalStatus globalStatus;

    synchronized public PlcStatus getPlcStatus() {
        return plcStatus;
    }

    synchronized public void setPlcStatus(PlcStatus plcStatus) {
        if(plcStatus != this.plcStatus) {
            PlcStatus OldStatus = this.plcStatus;
            this.plcStatus = plcStatus;
            for(StatusManagerListener l: this.listeners){
                l.onPLCStatusChange(OldStatus,this.plcStatus);
            }
            Logger.info("setPlcStatus: NEW STATUS ",plcStatus);
        }
        else
            Logger.warn("setPlcStatus: SAME STATUS ",plcStatus);
    }

    synchronized public LocalDbStatus getLocalDbStatus() {
        return localDbStatus;
    }

    synchronized public void setLocalDbStatus(LocalDbStatus localDbStatus) {
        if(localDbStatus != this.localDbStatus) {
            LocalDbStatus OldStatus = this.localDbStatus;
            this.localDbStatus = localDbStatus;
            for(StatusManagerListener l: this.listeners){
                l.onLocalDbStatusChange(OldStatus,this.localDbStatus);
            }
            Logger.info("setLocalDbStatus: NEW STATUS ",localDbStatus);
        }
        else
            Logger.warn("setLocalDbStatus: SAME STATUS ",localDbStatus);
    }

    synchronized public GlobalDbStatus getGlobalDbStatus() {
        return globalDbStatus;
    }

    synchronized public void setGlobalDbStatus(GlobalDbStatus globalDbStatus) {
        if(globalDbStatus != this.globalDbStatus) {
            GlobalDbStatus OldStatus = this.globalDbStatus;
            this.globalDbStatus = globalDbStatus;
            for(StatusManagerListener l: this.listeners){
                l.onGlobalDbStatusChange(OldStatus,this.globalDbStatus);
            }
            Logger.info("setGlobalDbStatus: NEW STATUS ",globalDbStatus);
        }
        else
            Logger.warn("setGlobalDbStatus: SAME STATUS ",globalDbStatus);
    }

    synchronized public GlobalStatus getGlobalStatus() {
        return globalStatus;
    }

    synchronized public void setGlobalStatus(GlobalStatus globalStatus) {
        if(globalStatus != this.globalStatus) {
            GlobalStatus OldStatus = this.globalStatus;
            this.globalStatus = globalStatus;
            for(StatusManagerListener l: this.listeners){
                l.onGlobalStatusChange(OldStatus,this.globalStatus);
            }
            Logger.info("setGlobalStatus: NEW STATUS ",globalStatus);
        }
        else
            Logger.warn("setGlobalStatus: SAME STATUS ",globalStatus);
    }
}
