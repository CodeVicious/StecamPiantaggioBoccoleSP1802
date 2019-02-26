package stecamSP1802.services;

public interface StatusManagerListener {
    public void onPLCStatusChange(StatusManager.PlcStatus oldStatus);
    public void onGlobalDbStatusChange(StatusManager.GlobalDbStatus globalDbStatus);
    public void onLocalDbStatusChange(StatusManager.LocalDbStatus localDbStatus);
    public void onGlobalStatusChange(StatusManager.GlobalStatus globalStatus);
}
