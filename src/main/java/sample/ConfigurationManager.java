package sample;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigurationManager {
    private static ConfigurationManager ourInstance = new ConfigurationManager();

    final Properties prop = new Properties();
    private InputStream input;
    private String plcName;


    private String plcIP;
    private int byteArrayPcPlc;
    private int byteArrayPlcPc;
    private int  dbNumberPcPlc;
    private int dbNumberPlcPc;
    private String comPORT;


    public static ConfigurationManager getInstance() {
        return ourInstance;
    }

    private ConfigurationManager() {
    }

    public void getConfiguration(){

        input = getClass().getResourceAsStream("/config.properties");

        try {
            prop.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }

        plcName = prop.getProperty("PLCName");
        plcIP = prop.getProperty("PLCIP");
        byteArrayPcPlc = Integer.parseInt(prop.getProperty("byteArrayWithLengthofDB-PLCPC"));
        byteArrayPlcPc = Integer.parseInt(prop.getProperty("byteArrayLengthofDB-PLCPC"));
        dbNumberPcPlc = Integer.parseInt(prop.getProperty("dbNumber-PLCPC"));
        dbNumberPlcPc = Integer.parseInt(prop.getProperty("dbNumber-PCPLC"));

        comPORT = prop.getProperty("COM");

    }

    public String getPlcName() {
        return plcName;
    }

    public String getPlcIP() {
        return plcIP;
    }

    public int getByteArrayPcPlc() {
        return byteArrayPcPlc;
    }

    public int getByteArrayPlcPc() {
        return byteArrayPlcPc;
    }

    public int getDbNumberPcPlc() {
        return dbNumberPcPlc;
    }

    public int getDbNumberPlcPc() {
        return dbNumberPlcPc;
    }

    public String getCOMPort() { return comPORT;  }
}
