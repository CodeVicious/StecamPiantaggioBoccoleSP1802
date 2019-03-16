package stecamSP1802.services;

import stecamSP1802.controllers.MainController;

public class StatusManagerListenerImp implements stecamSP1802.services.StatusManagerListener {

    final MainController mainController;
    private final PlcService plcService;

    public StatusManagerListenerImp(final MainController mainController, PlcService plcService) {
        this.mainController = mainController;
        this.plcService = plcService;
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
        if (globalDbStatus == StatusManager.GlobalDbStatus.GLOBAL_DB_DISCONNECTED) {
            mainController.gDbDisconnected();
        }
        if (globalDbStatus == StatusManager.GlobalDbStatus.GLOBAL_DB_CONNECTED) {
            mainController.gDbConnected();
        }
    }

    public void onLocalDbStatusChange(StatusManager.LocalDbStatus localDbStatus) {
        if (localDbStatus == StatusManager.LocalDbStatus.LOCAL_DB_DISCONNECTED) {
            mainController.lDbDisconnected();
        }
        if (localDbStatus == StatusManager.LocalDbStatus.LOCAL_DB_CONNECTED) {
            mainController.lDbConnected();
        }
    }

    public synchronized void onGlobalStatusChange(StatusManager.GlobalStatus globalStatus) {
        switch (globalStatus) {
            case WAITING_UDM:
                mainController.showMesage("SPARARE I CODICI UDM");
                break;
            case WAITING_WO:
                mainController.showMesage("IN ATTESA DI WORK ORDER");
                break;
            case WAITING_CODICE_RICETTA:
                mainController.showMesage("IN ATTESA DI CODICE RICETTA");
                break;
            case WAITING_CODICE_COMPONENTE:
                mainController.showMesage("IN ATTESA DI CODICE COMPONENTE");
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

        mainController.setUpControls(globalStatus);

    }
}
