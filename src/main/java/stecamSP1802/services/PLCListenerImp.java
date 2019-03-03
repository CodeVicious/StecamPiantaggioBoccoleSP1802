package stecamSP1802.services;

import com.google.common.base.Preconditions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import stecamSP1802.controllers.MainController;

public class PLCListenerImp implements PLCListener {
    private Logger Logger = LogManager.getLogger(PLCListenerImp.class);
    private final MainController mainController;
    private final StatusManager statusManager;

    public PLCListenerImp(final MainController mainController, final StatusManager statusManager) {
        Preconditions.checkNotNull(statusManager);
        Preconditions.checkNotNull(mainController);
        this.mainController = mainController;
        this.statusManager = statusManager;
    }

    public void onPLCBitChanged(int address, int pos, boolean val, String plcName) {
        Logger.info("PLC BIT CHANGED - PLC[" + plcName + "] - address [" + address + "] - pos [" + pos + "] - val[" + val + "]");
        if ((address == 0) && (val == true)) {
            switch (pos) {
                case 6: //Ok Ricetta, atteso lettura UDM
                    if (statusManager.getGlobalStatus() == StatusManager.GlobalStatus.WAITING_WO) {
                        statusManager.setGlobalStatus(StatusManager.GlobalStatus.WAITING_UDM);
                        mainController.onRicettaOK();
                    } else {
                        Logger.error("RICEVUTO 0.6 - Riceta OK inatteso");
                    }
                    break;
                case 5:
                    if (statusManager.getGlobalStatus() == StatusManager.GlobalStatus.WAITING_WO) {
                        mainController.onRicettaKO();
                    } else {
                        Logger.error("RICEVUTO 0.5 - Riceta KO inatteso");
                    }
                    break;
                case 4:
                    if (statusManager.getGlobalStatus() == StatusManager.GlobalStatus.WORKING) {
                        mainController.piantaggioBUONO();
                    } else {
                        Logger.error("RICEVUTO 0.4 - BUONO inatteso");
                    }
                    break;
                case 3:
                    if (statusManager.getGlobalStatus() == StatusManager.GlobalStatus.WORKING) {
                        mainController.piantaggioSCARTO();
                    } else {
                        Logger.error("RICEVUTO 0.3s - SCARTO inatteso");
                    }

            }
        }
    }
}