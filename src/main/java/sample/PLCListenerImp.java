package sample;

public class PLCListenerImp implements PLCListener {
    final MainController mainController;

    public PLCListenerImp(final MainController mainController) {
        this.mainController = mainController;
    }

    public void onPLCBitChanged(int address, int pos, boolean val, String plcName) {
        mainController.firePLCBitChange(address,pos,val,plcName);

    }
}
