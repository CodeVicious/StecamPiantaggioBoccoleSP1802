package stecamSP1802.services;

public interface StatusManagerListener {
    public void onPLCStatusChange(StatusManager.PlcStatus oldStatus);
    public void onGlobalDbStatusChange(StatusManager.GlobalDbStatus oldStatus, StatusManager.GlobalDbStatus globalDbStatus);
    public void onLocalDbStatusChange(StatusManager.LocalDbStatus oldStatus, StatusManager.LocalDbStatus plcStatus);
    public void onGlobalStatusChange(StatusManager.GlobalStatus oldStatus, StatusManager.GlobalStatus globalStatus);
}
