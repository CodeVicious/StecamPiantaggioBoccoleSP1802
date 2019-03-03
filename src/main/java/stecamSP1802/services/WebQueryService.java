package stecamSP1802.services;

import com.google.common.base.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import stecamSP1802.ConfigurationManager;
import stecamSP1802.services.barcode.Parte;
import stecamSP1802.services.barcode.WorkOrder;
import stecamSP1802.services.csvparser.CsvParserService;

import java.io.*;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class WebQueryService {
    final static Logger Logger = LogManager.getLogger(WebQueryService.class);
    ConfigurationManager conf = ConfigurationManager.getInstance();

    private CsvParserService csvParserService;


    private WorkOrder WO;
    private StatusManager statusManager;

    public WebQueryService(StatusManager statusManager) {
        Preconditions.checkNotNull(statusManager);
        csvParserService = new CsvParserService();
        this.statusManager = statusManager;
    }


    synchronized public String VerificaListaPartiWO(String barCode) {

        try {

            URL urlWO = new URL(conf.getVerificaListaPartiWOURL()+
                    "?NumeroWO="+barCode+
                    "&NomeStazione="+conf.getNomeStazione());

            URLConnection yc = urlWO.openConnection();

            yc.setRequestProperty("Accept-Charset", StandardCharsets.UTF_8.name());

            InputStream is  = yc.getInputStream();

            Logger.info("Parsing ");
            if (csvParserService.parse(is).matches("OK")) {
                WO = csvParserService.getWO();
                Logger.info("WAITING OK RICETTA DAL PLC ");
                return WO.getCodiceRicetta();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        //iFile = getClass().getResourceAsStream("/WO2.csv");

        // Once you have the Input Stream, it's just plain old Java IO stuff.

        // For this case, since you are interested in getting plain-text web page
        // I'll use a reader and output the text content to System.out.

        // For binary content, it's better to directly read the bytes from stream and write
        // to the target file.

        //


        return "KO";
    }

    public Boolean VerificaUDM(String barCode) {

        try {
            URL urlUDM = new URL(conf.getVerificaListaPartiUDM()+
                    "?UdM="+barCode
                    );
            URLConnection con = urlUDM.openConnection();

            BufferedReader inputStreamReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line = inputStreamReader.readLine();

            if (line.matches("KO")) {
                Logger.error("KO AS FIRST LINE - FIRST LINE " + line + "");
                return false;
            } else if (line.matches("OK")) {
                line = inputStreamReader.readLine();
                if(WO.getListaParti().containsKey(line)) {
                    Parte codice = WO.getListaParti().get(line);
                    codice.setVerificato(true);
                    Logger.info("UDM OK - CODE " + line);
                    return true;
                }
            } else {
                Logger.info("CODICE NON PRESENTE IN LISTA");
                return false;
            }

            return false;

        } catch (IOException e) {
            Logger.error(e);
            return false;
        }
    }

    public Boolean checkValidazioneUDM() {
        return WO.checkLavorabile();
    }

    public WorkOrder getWO() {
        return WO;
    }


    public void sendAbilitaUDM() {
        try {
            URL urlVERICFICA = new URL(conf.getuRLVerificaUID());
            URLConnection con = urlVERICFICA.openConnection();
            InputStream is = con.getInputStream();
        } catch (IOException e) {
            Logger.error(e);
        }
    }

    public void sendDisabilitaUDM() {

        try {
            URL urlVERICFICA = new URL(conf.getuRLVerificaUID());
            URLConnection con = urlVERICFICA.openConnection();
            InputStream is = con.getInputStream();
        } catch (IOException e) {
            Logger.error(e);
        }
    }

    public Map<String, Parte> getParti() {
        return WO.getListaParti();
    }
}
