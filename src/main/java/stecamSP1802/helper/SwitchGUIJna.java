package stecamSP1802.helper;

import com.google.common.collect.Maps;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

import com.sun.jna.platform.win32.WinDef.HWND;


import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.win32.StdCallLibrary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.util.Map;


public class SwitchGUIJna {
    static Logger Logger = LogManager.getLogger(SwitchGUIJna.class);

    static Map<String,HWND> listaWin = Maps.newHashMap();

    public interface User32 extends StdCallLibrary {
        User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class);

        boolean EnumWindows(WinUser.WNDENUMPROC lpEnumFunc, Pointer arg);
        boolean ShowWindow(HWND hWnd, int nCmdShow);

        HWND SetFocus(HWND hWnd);

        int GetWindowTextA(HWND hWnd, byte[] lpString, int nMaxCount);

        boolean SetForegroundWindow(HWND hWnd);
    }


    public static void SwitchGUI(String switchTo) {
        listaWin.clear();
        final User32 user32 = User32.INSTANCE;

        user32.EnumWindows(new WinUser.WNDENUMPROC() {
            @Override
            public boolean callback(HWND hWnd, Pointer data) {
                byte[] windowText = new byte[1024];
                user32.GetWindowTextA(hWnd, windowText, windowText.length);
                String wText = Native.toString(windowText);

                if (wText.isEmpty()) {
                    return true;
                }

                listaWin.put(wText,hWnd);

                return true;
            }
        }, null);

        for(String t: listaWin.keySet()) {
            Logger.info(t);
            if (t.equals(switchTo)) {
                user32.SetForegroundWindow(listaWin.get(t));
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Logger.info("SWITCH ");
                    System.out.println("SWITCH");
                }
                Logger.info("SWITCH ");
                System.out.println("SWITCH");
                user32.ShowWindow(listaWin.get(t), WinUser.SW_SHOWNOACTIVATE);
                System.out.println(t);
                return;
            }
        }
    }
}