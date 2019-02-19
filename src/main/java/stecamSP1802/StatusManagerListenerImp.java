package stecamSP1802;

import stecamSP1802.services.StatusManager;
import stecamSP1802.services.StatusManagerListener;

public class StatusManagerListenerImp implements stecamSP1802.services.StatusManagerListener {

    final MainController mainController;
    public StatusManagerListenerImp(final MainController mainController){
        this.mainController = mainController;
    }
    public void onPLCStatusChange(StatusManager.PlcStatus oldStatus, StatusManager.PlcStatus plcStatus) {
        if(plcStatus == StatusManager.PlcStatus.PLC_CONNECTING){
            mainController.plcDisconnected();
        }
        if(plcStatus == StatusManager.PlcStatus.PLC_CONNECTED){
            mainController.plcConnected();
        }
    }

    public void onGlobalDbStatusChange(StatusManager.GlobalDbStatus oldStatus, StatusManager.GlobalDbStatus globalDbStatus) {
        if(globalDbStatus == StatusManager.GlobalDbStatus.GLOBAL_DB_CONNECTING){
            mainController.gDbDisconnected();
        }
        if(globalDbStatus == StatusManager.GlobalDbStatus.GLOBAL_DB_CONNECTED){
            mainController.gDbConnected();
        }
    }

    public void onLocalDbStatusChange(StatusManager.LocalDbStatus oldStatus, StatusManager.LocalDbStatus localDbStatus) {
        if( localDbStatus == StatusManager.LocalDbStatus.LOCAL_DB_CONNECTING ){
            mainController.lDbDisconnected();
        }
        if( localDbStatus == StatusManager.LocalDbStatus.LOCAL_DB_CONNECTED ){
            mainController.lDbConnected();
        }
    }

    public void onGlobalStatusChange(StatusManager.GlobalStatus oldStatus, StatusManager.GlobalStatus globalStatus) {

    }
}
