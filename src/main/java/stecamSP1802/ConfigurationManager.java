package stecamSP1802;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import stecamSP1802.helper.PasswordMD5Converter;
import stecamSP1802.services.DbService;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;


public class ConfigurationManager { //Singleton
    private static Logger Logger = LogManager.getLogger(ConfigurationManager.class);
    private static ConfigurationManager ourInstance = new ConfigurationManager();

    public static ConfigurationManager getInstance() {
        return ourInstance;
    }

    private ConfigurationManager() {
    }


    //From Local DB
    Map<String, String> param;
    private DbService dbService;

    private String localAdminUser;
    private String localUser;
    private String localAdminPassword;

    private String verificaListaPartiWOURL;
    private String verificaListaPartiUDM;
    private String nomeStazione;
    private String uRLVerificaUID;
    private String uRLDisabilitaUDM;
    private String connessioneSERVER;

    private String passwordAmministrativa;
    private String utenteLocale;
    private int logoffTimeout;


    //From config properties
    final Properties prop = new Properties();

    private String plcName;
    private String plcIP;
    private int byteArrayPcPlc;
    private int byteArrayPlcPc;
    private int dbNumberPcPlc;
    private int dbNumberPlcPc;
    public  String connessioneLOCALSERVER;
    private String comPORT;
    private double[] bitMonitor;
    private String JavaUi;
    private String Hmi;


    public void getFileConfiguration() {
        try {
            File jarFile = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());

            InputStream input = new FileInputStream(jarFile.getParentFile() + "/main.properties");

            prop.load(input);
        } catch (IOException | URISyntaxException e) {
            Logger.error(e);
        }


        //Load Properties from file
        plcName = prop.getProperty("PLCName");
        plcIP = prop.getProperty("PLCIP");
        byteArrayPcPlc = Integer.parseInt(prop.getProperty("byteArrayLengthofDB-PC-PLC"));
        byteArrayPlcPc = Integer.parseInt(prop.getProperty("byteArrayLengthofDB-PLC-PC"));
        dbNumberPcPlc = Integer.parseInt(prop.getProperty("dbNumber-PC-PLC"));
        dbNumberPlcPc = Integer.parseInt(prop.getProperty("dbNumber-PLC-PC"));
        String[] bitArray = prop.get("bitMonitor").toString().split("#");
        bitMonitor = new double[bitArray.length];
        for (int i = 0; i < bitArray.length; i++)
            bitMonitor[i] = Double.parseDouble(bitArray[i]);
        comPORT = prop.getProperty("COM");
        connessioneLOCALSERVER = prop.getProperty("ConnessioneLOCALSERVER");
        JavaUi = prop.getProperty("JAVA-UI");
        Hmi = prop.getProperty("HMI");

    }

    public void getDBConfiguration(){
        if(dbService==null)
        {
            Logger.error("DBService NULL");
            return;
        }

        try {
            param = dbService.queryParametri();
        } catch (SQLException e) {
            Logger.error(e);
        }

        //Load properties from db
        verificaListaPartiWOURL = param.get("VerificaListaPartiWOURL");
        verificaListaPartiUDM  = param.get("VerificaListaPartiUDM");

        nomeStazione =  param.get("NomeStazione");
        uRLDisabilitaUDM = param.get("URLDisabilitaUDM");
        connessioneSERVER = param.get("ConnessioneSERVER");

        passwordAmministrativa = param.get("PasswordAmministrativa");
        utenteLocale = param.get("utenteLocale");
        logoffTimeout = Integer.parseInt(param.get("LogoffTimeout"));

        localAdminUser = param.get("utente-Amministratore");
        localUser = param.get("utente-Locale");
        localAdminPassword = param.get("password-Amministratore");
    }

    public Properties getPropFile() {
        return prop;
    }

    public Map<String, String> getPropDB() {
        return param;
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

    public String getCOMPort() {
        return comPORT;
    }

    public double[] getBitMonitor() {
        return bitMonitor;
    }

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

    public String getJavaUi(){ return JavaUi;}

    public String getHmi(){return Hmi;}

    public String getPasswordAmministrativa() {
        return passwordAmministrativa;
    }

    public String getUtenteLocale() {
        return utenteLocale;
    }

    public int getLogoffTimeout() {
        return logoffTimeout;
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

        //Salvo parametri file
        try {
            File jarFile = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
            FileOutputStream output = new FileOutputStream(jarFile.getParentFile() + "/main.properties");

            prop.store(output, null);

        } catch (IOException | URISyntaxException e) {
            Logger.error(e);
        }

        //salvo parametri DB
        try {
            dbService.saveParametri( param);
        } catch (SQLException e) {
            Logger.error(e);
        }

    }

    public boolean checkMatricola(String matricola) {
        if (localAdminUser.matches(matricola) || localUser.matches(matricola))
            return true;
        return false;
    }

    public boolean isLocalAdmin(String matricola) {
        if (localAdminUser.matches(matricola))
            return true;
        return false;
    }

    public boolean checkPassword(String password) {
        if (localAdminPassword.matches(PasswordMD5Converter.getMD5(password)))
            return true;
        return false;
    }

    public void setDbService(DbService dbService) {
        this.dbService = dbService;
    }
}
