package stecamSP1802.services;

import com.google.common.base.Preconditions;
import stecamSP1802.MainController;

public class PLCListenerImp implements PLCListener {
    private final MainController mainController;
    private final StatusManager statusManager;

    public PLCListenerImp(final MainController mainController, final StatusManager statusManager) {
        Preconditions.checkNotNull(statusManager);
        Preconditions.checkNotNull(mainController);
        this.mainController = mainController;
        this.statusManager = statusManager;
    }

    public void onPLCBitChanged(int address, int pos, boolean val, String plcName) {
        if ((address == 0) && (val == true)) {
            switch (pos) {
                case 6: //Ok Ricetta, atteso lettura UDM
                    statusManager.setGlobalStatus(StatusManager.GlobalStatus.WAITING_UDM);
                    mainController.onRicettaOK();
                    break;
                case 5:
                    mainController.onRicettaKO();
                    break;
                case 4:
                    mainController.piantaggioBUONO();
                    break;
                case 3:
                    mainController.piantaggioSCARTO();

            }
        }
    }
}