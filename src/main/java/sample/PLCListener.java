package sample;

public interface PLCListener {
    public void onPLCBitChanged(int address, int pos, boolean val, String plcName);
}
