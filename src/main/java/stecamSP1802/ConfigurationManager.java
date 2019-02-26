package stecamSP1802;


import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigurationManager {
    private static ConfigurationManager ourInstance = new ConfigurationManager();
    private String verificaListaPartiWOURL;
    private String verificaListaPartiUDM;
    private String nomeStazione;
    private String uRLVerificaUID;
    private String uRLDisabilitaUDM;
    private String connessioneSERVER;
    private String connessioneLOCALSERVER;
    private String passwordAmministrativa;
    private String utenteLocale;
    private int logoffTimeout;

    public static ConfigurationManager getInstance() {
        return ourInstance;
    }
    private ConfigurationManager() {}

    final Properties prop = new Properties();


    private InputStream input;
    private String plcName;


    private String plcIP;
    private int byteArrayPcPlc;
    private int byteArrayPlcPc;
    private int  dbNumberPcPlc;
    private int dbNumberPlcPc;
    private String comPORT;
    private double[] bitMonitor;



    public void getConfiguration(){

        input = getClass().getResourceAsStream("/config.properties");

        try {
            prop.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }

        plcName = prop.getProperty("PLCName");
        plcIP = prop.getProperty("PLCIP");
        byteArrayPcPlc = Integer.parseInt(prop.getProperty("byteArrayLengthofDB-PC-PLC"));
        byteArrayPlcPc = Integer.parseInt(prop.getProperty("byteArrayLengthofDB-PLC-PC"));
        dbNumberPcPlc = Integer.parseInt(prop.getProperty("dbNumber-PC-PLC"));
        dbNumberPlcPc = Integer.parseInt(prop.getProperty("dbNumber-PLC-PC"));
        String[]bitArray = prop.get("bitMonitor").toString().split("#");
        bitMonitor = new double[bitArray.length];
        for(int i=0;i<bitArray.length;i++)
            bitMonitor[i]=Double.parseDouble(bitArray[i]);

        comPORT = prop.getProperty("COM");

        verificaListaPartiWOURL = prop.getProperty("VerificaListaPartiWOURL");
        verificaListaPartiUDM  = prop.getProperty("VerificaListaPartiUDM");

        nomeStazione =  prop.getProperty("NomeStazione");
        uRLDisabilitaUDM = prop.getProperty("URLDisabilitaUDM");
        connessioneSERVER = prop.getProperty("ConnessioneSERVER");
        connessioneLOCALSERVER = prop.getProperty("ConnessioneLOCALSERVER");
        passwordAmministrativa = prop.getProperty("PasswordAmministrativa");
        utenteLocale = prop.getProperty("utenteLocale");
        logoffTimeout = Integer.parseInt(prop.getProperty("LogoffTimeout"));

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

    public String getJDBCString() {
        return "jdbc:sqlserver://127.0.0.1;databaseName=StecamSP1802;user=sqluser;password=sqluser";
    }

    public double[] getBitMonitor() { return bitMonitor; }

    public String getVerificaListaPartiWOURL() {
        return verificaListaPartiWOURL;
    }

    public String getVerificaListaPartiUDM() {
        return verificaListaPartiUDM;
    }

    public String getNomeStazione() {
        return nomeStazione;
    }

    public String getuRLVerificaUID() {
        return uRLVerificaUID;
    }

    public String getuRLDisabilitaUDM() {
        return uRLDisabilitaUDM;
    }

    public String getConnessioneSERVER() {
        return connessioneSERVER;
    }

    public String getConnessioneLOCALSERVER() {
        return connessioneLOCALSERVER;
    }

    public String getPasswordAmministrativa() {
        return passwordAmministrativa;
    }

    public String getUtenteLocale() {
        return utenteLocale;
    }

    public int getLogoffTimeout() {
        return logoffTimeout;
    }

    public Properties getProp() {
        return prop;
    }
}
