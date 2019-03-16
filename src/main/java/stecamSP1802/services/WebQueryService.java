package stecamSP1802.services;

import com.google.common.base.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import stecamSP1802.ConfigurationManager;
import stecamSP1802.controllers.MainController;
import stecamSP1802.services.barcode.WorkOrder;
import stecamSP1802.services.csvparser.CsvParserService;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public class WebQueryService {
    final static Logger Logger = LogManager.getLogger(WebQueryService.class);
    ConfigurationManager conf = ConfigurationManager.getInstance();

    private CsvParserService csvParserService;
    private StatusManager statusManager;
    private MainController mainController;
    private boolean isWebOffline = false;

    public WebQueryService(StatusManager statusManager, MainController mainController) {
        Preconditions.checkNotNull(statusManager);
        csvParserService = new CsvParserService();
        this.statusManager = statusManager;
        this.mainController = mainController;
    }


    synchronized public EsitoWebQuery VerificaListaPartiWO(String barCode) {

        URL urlWO = null;
        try {
            urlWO = new URL(conf.getVerificaListaPartiWOURL() +
                    "?NumeroWO=" + barCode +
                    "&NomeStazione=" + conf.getNomeStazione());

            URLConnection yc = urlWO.openConnection();

            setWebOffline(false);

            //yc.setRequestProperty("Accept-Charset", StandardCharsets.UTF_8.name());
            InputStream is = yc.getInputStream();

            BufferedReader inputStreamReader = new BufferedReader(new InputStreamReader(is));
            String line = inputStreamReader.readLine();
            if (!line.matches("OK")) {
                line = inputStreamReader.readLine();
                Logger.error("VerificaListapartiWO - KO " + line);
                return new EsitoWebQuery(EsitoWebQuery.ESITO.KO, line);
            }
            csvParserService.parse(inputStreamReader);
            csvParserService.fillWO();
            Logger.info("ESITO LIST WO OK ");
            return new EsitoWebQuery(EsitoWebQuery.ESITO.OK, WorkOrder.getInstance().getCodiceRicetta());

        } catch (MalformedURLException e) {
            Logger.error("l'URL " + conf.getVerificaListaPartiWOURL() + " è sbagliato" + e);
            setWebOffline(true);
            return new EsitoWebQuery(EsitoWebQuery.ESITO.KO, "Problemi nell'URL");


        } catch (IOException e) {
            Logger.error("Errore di accesso all'URL " + conf.getVerificaListaPartiWOURL() + " " + e);
            setWebOffline(true);
            return new EsitoWebQuery(EsitoWebQuery.ESITO.KO, "Problemi di comunicazione");
        }

    }

    public EsitoWebQuery VerificaUDM(String barCode) {

        try {
            URL urlUDM = new URL(conf.getVerificaListaPartiUDM() +
                    "?UdM=" + barCode
            );
            URLConnection con = urlUDM.openConnection();

            setWebOffline(false);

            BufferedReader inputStreamReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line = inputStreamReader.readLine();

            if (line.matches("KO")) {
                Logger.error("KO AS FIRST LINE - FIRST LINE " + line + "");
                line = inputStreamReader.readLine();
                return new EsitoWebQuery(EsitoWebQuery.ESITO.KO, line);
            } else {
                line = inputStreamReader.readLine();
                return new EsitoWebQuery(EsitoWebQuery.ESITO.OK, line);
            }

        } catch (MalformedURLException e) {
            Logger.error("l'URL " + conf.getVerificaListaPartiUDM() + " è sbagliato" + e);
            setWebOffline(true);
            return null;
        } catch (IOException e) {
            Logger.error("Errore di accesso all'URL " + conf.getVerificaListaPartiUDM() + " " + e);
            setWebOffline(true);
            return null;
        }

    }


    public void sendAbilitaUDM() {
        try {
            URL urlVERICFICA = new URL(conf.getuRLDisabilitaUDM() +
                    "?NomeStazione=" + conf.getNomeStazione() +
                    "&DisabilitaConvertiUDM=NO");

            URLConnection con = urlVERICFICA.openConnection();
            InputStream is = con.getInputStream();
            setWebOffline(false);
        } catch (MalformedURLException e) {
            Logger.error("l'URL " + conf.getuRLDisabilitaUDM() + " è sbagliato" + e);
            setWebOffline(true);

        } catch (IOException e) {
            Logger.error("Errore di accesso all'URL " + conf.getuRLDisabilitaUDM() + " " + e);
            setWebOffline(true);

        }
    }

    public void sendDisabilitaUDM() {

        try {
            URL urlVERICFICA = new URL(conf.getuRLDisabilitaUDM() +
                    "?NomeStazione=" + conf.getNomeStazione() +
                    "&DisabilitaConvertiUDM=SI");

            URLConnection con = urlVERICFICA.openConnection();
            InputStream is = con.getInputStream();
            setWebOffline(false);
        } catch (MalformedURLException e) {
            Logger.error("l'URL " + conf.getuRLDisabilitaUDM() + " è sbagliato" + e);
            setWebOffline(true);
        } catch (IOException e) {
            Logger.error("Errore di accesso all'URL " + conf.getuRLDisabilitaUDM() + " " + e);
            setWebOffline(true);
        }
    }


    public void checkSendUDM(boolean isWOListPartEnabled, boolean isUDMVerificaEnabled) {
        if (isUDMVerificaEnabled && isWOListPartEnabled) {
            sendAbilitaUDM();
        } else {
            sendDisabilitaUDM();
        }

    }

    public void cleanWO() {
        WorkOrder.getInstance().cleanUP();
    }

    public boolean isWebOffline() {
        return isWebOffline;
    }

    public void setWebOffline(boolean webOffline) {
        if (webOffline)
            mainController.setControlloOFFLine();
        isWebOffline = webOffline;
    }
}
