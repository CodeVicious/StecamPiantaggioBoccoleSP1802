package stecamSP1802.services;


import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class StatusManager {
    private static Logger Logger = LogManager.getLogger(StatusManager.class);

    private Boolean isServerConnectionDisabled;
    private List<StatusManagerListener> listeners;


    public static enum GlobalStatus {
        CONNECTING, CONNECTED, WAITING_WO, WAITING_UDM, WORKING
    }

    public static enum PlcStatus {
        PLC_CONNECTING, PLC_CONNECTED
    }

    public static enum LocalDbStatus {
        LOCAL_DB_CONNECTING, LOCAL_DB_CONNECTED, LOCAL_DB_DISABLED
    }

    public static enum GlobalDbStatus {
        GLOBAL_DB_CONNECTING, GLOBAL_DB_CONNECTED, GLOBAL_DB_DISABLED,
    }

    private PlcStatus plcStatus;
    private LocalDbStatus localDbStatus;
    private GlobalDbStatus globalDbStatus;
    private GlobalStatus globalStatus;

    public StatusManager() {
        listeners = Lists.newArrayList();
        isServerConnectionDisabled = false;
        plcStatus = PlcStatus.PLC_CONNECTING;
        localDbStatus = LocalDbStatus.LOCAL_DB_CONNECTING;
        globalDbStatus = GlobalDbStatus.GLOBAL_DB_CONNECTING;
        globalStatus = GlobalStatus.CONNECTING;
    }


    synchronized public PlcStatus getPlcStatus() {
        return plcStatus;
    }

    synchronized public void setPlcStatus(PlcStatus plcStatus) {

        for (StatusManagerListener l : this.listeners) {
            l.onPLCStatusChange(this.plcStatus);
        }
        Logger.info("setPlcStatus: NEW STATUS ", plcStatus);

    }

    synchronized public LocalDbStatus getLocalDbStatus() {
        return localDbStatus;
    }

    synchronized public void setLocalDbStatus(LocalDbStatus localDbStatus) {
        for (StatusManagerListener l : this.listeners) {
            l.onLocalDbStatusChange(this.localDbStatus);
        }
        Logger.info("setLocalDbStatus: NEW STATUS ", localDbStatus);
    }

    synchronized public GlobalDbStatus getGlobalDbStatus() {
        return globalDbStatus;
    }

    synchronized public void setGlobalDbStatus(GlobalDbStatus globalDbStatus) {
        for (StatusManagerListener l : this.listeners) {
            l.onGlobalDbStatusChange(this.globalDbStatus);
        }
        Logger.info("setGlobalDbStatus: NEW STATUS ", globalDbStatus);
    }

    synchronized public GlobalStatus getGlobalStatus() {
        return globalStatus;
    }

    synchronized public void setGlobalStatus(GlobalStatus globalStatus) {
        for (StatusManagerListener l : this.listeners) {
            l.onGlobalStatusChange(this.globalStatus);
        }
        Logger.info("setGlobalStatus: NEW STATUS ", globalStatus);
    }

    public void addListener(StatusManagerListener list) {
        listeners.add(list);
    }
}
