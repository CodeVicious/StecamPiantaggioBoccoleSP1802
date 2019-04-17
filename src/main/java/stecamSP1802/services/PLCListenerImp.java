package stecamSP1802.services;

import com.google.common.base.Preconditions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import stecamSP1802.ConfigurationManager;
import stecamSP1802.controllers.MainController;
import stecamSP1802.helper.SwitchGUIJna;

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
                case 7: //Switch GUI
                    Logger.info("RICEVUTO 0.7 - CAMBIO GUI");
                    SwitchGUIJna.SwitchGUI((ConfigurationManager.getInstance().getJavaUi()));

                    break;
                case 6: //Ok Ricetta, atteso lettura UDM
                    if (  statusManager.getGlobalStatus()== StatusManager.GlobalStatus.WAITING_RICETTA_OK_KO){
                        statusManager.setGlobalStatus(StatusManager.GlobalStatus.WAITING_UDM);
                        mainController.onRicettaOK();
                    } else {
                        Logger.error("RICEVUTO 0.6 - Ricetta OK inatteso");
                    }
                    break;
                case 5:// KO Ricetta
                    if (  statusManager.getGlobalStatus()== StatusManager.GlobalStatus.WAITING_RICETTA_OK_KO){
                        mainController.onRicettaKO();
                    } else {
                        Logger.error("RICEVUTO 0.5 - Ricetta KO inatteso");
                    }
                    break;
                case 4:// BUONO
                    if (statusManager.getGlobalStatus() == StatusManager.GlobalStatus.WORKING) {
                        mainController.piantaggioBUONO();
                    } else {
                        Logger.error("RICEVUTO 0.4 - BUONO inatteso");
                    }
                    break;
                case 3:// SCARTO
                    if (statusManager.getGlobalStatus() == StatusManager.GlobalStatus.WORKING) {
                        mainController.piantaggioSCARTO();
                    } else {
                        Logger.error("RICEVUTO 0.3s - SCARTO inatteso");
                    }

            }
        }
    }
}