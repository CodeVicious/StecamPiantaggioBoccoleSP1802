package stecamSP1802;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import stecamSP1802.helper.PasswordMD5Converter;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;


public class ConfigurationManager { //Singleton
    private static Logger Logger = LogManager.getLogger(ConfigurationManager.class);
    private static ConfigurationManager ourInstance = new ConfigurationManager();
    public static ConfigurationManager getInstance() {
        return ourInstance;
    }
    private ConfigurationManager() {}


    private String localAdminUser;
    private String localUser;
    private String localAdminPassword;


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

        localAdminUser = prop.getProperty("utente-Amministratore");
        localUser = prop.getProperty("utente-Locale");
        localAdminPassword = prop.getProperty("password-Amministratore");

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

    public String getLocalAdminUser() {
        return localAdminUser;
    }

    public String getLocalAdminPassword() {
        return localAdminPassword;
    }

    public String getLocalUser() {
        return localUser;
    }

    public void saveProperties() {
        URL resourceUrl = getClass().getResource("/config.properties");
        File file = null;
        try {
            file = new File(resourceUrl.toURI());
            FileOutputStream output = new FileOutputStream(file);
            prop.store(output,null);

        } catch (URISyntaxException | IOException e) {
            Logger.error(e);
        }

    }

    public boolean checkMatricola(String matricola) {
        if(localAdminUser.matches(matricola) || localUser.matches(matricola))
            return true;
        return false;
    }

    public boolean isLocalAdmin(String matricola) {
        if(localAdminUser.matches(matricola))
            return true;
        return false;
    }

    public boolean checkPassword(String password) {
        if (localAdminPassword.matches(PasswordMD5Converter.getMD5(password)))
            return true;
        return false;
    }
}
