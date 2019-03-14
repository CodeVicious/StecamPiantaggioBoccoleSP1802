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
    private boolean isWebOffline = false;

    public WebQueryService(StatusManager statusManager) {
        Preconditions.checkNotNull(statusManager);
        csvParserService = new CsvParserService();
        this.statusManager = statusManager;
    }


    synchronized public String VerificaListaPartiWO(String barCode) {

        URL urlWO = null;
        try {
            urlWO = new URL(conf.getVerificaListaPartiWOURL() +
                    "?NumeroWO=" + barCode +
                    "&NomeStazione=" + conf.getNomeStazione());

            URLConnection yc = urlWO.openConnection();

            isWebOffline = false;

            yc.setRequestProperty("Accept-Charset", StandardCharsets.UTF_8.name());
            InputStream is = yc.getInputStream();
            Logger.info("Parsing ");
            if (csvParserService.parse(is).matches("OK")) {
                WO = csvParserService.getWO();
                Logger.info("WAITING OK RICETTA DAL PLC ");
                return WO.getCodiceRicetta();
            }

        } catch (MalformedURLException e) {
            Logger.error("l'URL " + conf.getVerificaListaPartiWOURL() + " è sbagliato" + e);
            isWebOffline = true;
        } catch (IOException e) {
            Logger.error("Errore di accesso all'URL " + conf.getVerificaListaPartiWOURL() + " " + e);
            isWebOffline = true;
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

    public Boolean VerificaUDM(String barCode, boolean isWOListPartEnabled) {

        try {
            URL urlUDM = new URL(conf.getVerificaListaPartiUDM() +
                    "?UdM=" + barCode
            );
            URLConnection con = urlUDM.openConnection();

            isWebOffline = false;

            BufferedReader inputStreamReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line = inputStreamReader.readLine();

            if (line.matches("KO")) {
                Logger.error("KO AS FIRST LINE - FIRST LINE " + line + "");
                return false;
            } else if (line.matches("OK")) {
                line = inputStreamReader.readLine();
                if (isWOListPartEnabled) {
                    if (WO.getListaParti().containsKey(line)) {
                        Parte codice = WO.getListaParti().get(line);
                        codice.setVerificato(true);
                        Logger.info("UDM OK - CODE " + line);
                    }
                    return true;
                }

            } else {
                Logger.info("CODICE NON PRESENTE IN LISTA");
                return false;
            }

            return false;

        } catch (MalformedURLException e) {
            Logger.error("l'URL " + conf.getVerificaListaPartiUDM() + " è sbagliato" + e);
            isWebOffline = true;
            return false;
        } catch (IOException e) {
            Logger.error("Errore di accesso all'URL " + conf.getVerificaListaPartiUDM() + " " + e);
            isWebOffline = true;
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
            URL urlVERICFICA = new URL(conf.getuRLVerificaUID() +
                    "?NomeStazione=" + conf.getNomeStazione() +
                    "&DisabilitaConvertiUDM=NO");

            URLConnection con = urlVERICFICA.openConnection();
            InputStream is = con.getInputStream();
            isWebOffline = false;
        } catch (MalformedURLException e) {
            Logger.error("l'URL " + conf.getVerificaListaPartiUDM() + " è sbagliato" + e);
            isWebOffline = true;

        } catch (IOException e) {
            Logger.error("Errore di accesso all'URL " + conf.getVerificaListaPartiUDM() + " " + e);
            isWebOffline = true;

        }
    }

    public void sendDisabilitaUDM() {

        try {
            URL urlVERICFICA = new URL(conf.getuRLVerificaUID() +
                    "?NomeStazione=" + conf.getNomeStazione() +
                    "&DisabilitaConvertiUDM=SI");

            URLConnection con = urlVERICFICA.openConnection();
            InputStream is = con.getInputStream();
            isWebOffline = false;
        } catch (MalformedURLException e) {
            Logger.error("l'URL "+conf.getVerificaListaPartiUDM()+" è sbagliato" + e );
            isWebOffline = true;
        } catch (IOException e) {
            Logger.error("Errore di accesso all'URL "+conf.getVerificaListaPartiUDM() +" "+ e );
            isWebOffline = true;
        }
    }

    public Map<String, Parte> getParti() {
        return WO.getListaParti();
    }

    public void checkSendUDM(boolean isWOListPartEnabled, boolean isUDMVerificaEnabled) {
        if (isUDMVerificaEnabled && isWOListPartEnabled) {
            sendAbilitaUDM();
        } else {
            sendDisabilitaUDM();
        }

    }

    public void cleanWO() {
        WO.getListaParti().clear();
        WO.setDescrizione("");
        WO.setCodiceRicetta("");
        WO.setBarCodeWO("");
    }

    public boolean isWebOffline() {
        return isWebOffline;
    }
}
