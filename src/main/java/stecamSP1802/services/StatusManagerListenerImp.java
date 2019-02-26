package stecamSP1802.services;

import stecamSP1802.controllers.MainController;
import stecamSP1802.services.StatusManager;

public class StatusManagerListenerImp implements stecamSP1802.services.StatusManagerListener {

    final MainController mainController;

    public StatusManagerListenerImp(final MainController mainController) {
        this.mainController = mainController;
    }

    public void onPLCStatusChange(StatusManager.PlcStatus plcStatus) {
        if (plcStatus == StatusManager.PlcStatus.PLC_CONNECTING) {
            mainController.plcDisconnected();
        }
        if (plcStatus == StatusManager.PlcStatus.PLC_CONNECTED) {
            mainController.plcConnected();
        }
    }

    public void onGlobalDbStatusChange(StatusManager.GlobalDbStatus globalDbStatus) {
        if (globalDbStatus == StatusManager.GlobalDbStatus.GLOBAL_DB_CONNECTING) {
            mainController.gDbDisconnected();
        }
        if (globalDbStatus == StatusManager.GlobalDbStatus.GLOBAL_DB_CONNECTED) {
            mainController.gDbConnected();
        }
    }

    public void onLocalDbStatusChange(StatusManager.LocalDbStatus localDbStatus) {
        if (localDbStatus == StatusManager.LocalDbStatus.LOCAL_DB_CONNECTING) {
            mainController.lDbDisconnected();
        }
        if (localDbStatus == StatusManager.LocalDbStatus.LOCAL_DB_CONNECTED) {
            mainController.lDbConnected();
        }
    }

    public void onGlobalStatusChange(StatusManager.GlobalStatus globalStatus) {
        switch (globalStatus) {
            case WAITING_UDM:
                mainController.showMesage("SPARARE I CODICI UDM");
                break;
            case WAITING_WO:
                mainController.showMesage("IN ATTESA DI WORK ORDER");
                break;
            case WORKING:
                mainController.showMesage("PIANTAGGIO");
                break;
            case CONNECTING:
                mainController.showMesage("TRACCIAMENTO IN CONNESSIONE");
                break;
            case CONNECTED:
                mainController.showMesage("TRACCIAMENTO CONNESSO");
        }

    }
}