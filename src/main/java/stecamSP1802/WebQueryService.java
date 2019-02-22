package stecamSP1802;

import com.google.common.base.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import stecamSP1802.services.StatusManager;
import stecamSP1802.services.barcode.WorkOrder;
import stecamSP1802.services.csvparser.CsvParserService;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class WebQueryService {
    URL urlWO,urlUDM,urlVERICFICA;

    final static Logger Logger = LogManager.getLogger(WebQueryService.class);
    private CsvParserService csvParserService;
    private InputStream iFile;
    private InputStream is;

    private WorkOrder WO;
    private StatusManager statusManager;

    public WebQueryService(){
        csvParserService = new CsvParserService();

    }

    synchronized public void VerificaListaPartiWO(String barCode, StatusManager statusManager)  {
        Preconditions.checkNotNull(statusManager);

        try {
            urlWO = new URL(ConfigurationManager.getInstance().getVerificaListaPartiWOURL());
            urlUDM = new URL(ConfigurationManager.getInstance().getVerificaListaPartiUDM());
            urlVERICFICA  = new URL(ConfigurationManager.getInstance().getuRLVerificaUID());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        this.statusManager = statusManager;

        // Make a URL to the web page

        // Get the input stream through URL Connection

        /*
        URLConnection con = url.openConnection();
        InputStream is =con.getInputStream();
        */

        iFile = getClass().getResourceAsStream("/WO.csv");

        // Once you have the Input Stream, it's just plain old Java IO stuff.

        // For this case, since you are interested in getting plain-text web page
        // I'll use a reader and output the text content to System.out.

        // For binary content, it's better to directly read the bytes from stream and write
        // to the target file.

        //BufferedReader br = new BufferedReader(new InputStreamReader(is));



        Logger.info("Parsing ");
        if(csvParserService.parse(iFile)=="OK"){
            WO=csvParserService.getWO();
            Logger.info("WAITING UDM ");
            statusManager.setGlobalStatus(StatusManager.GlobalStatus.WAITING_UDM);
        }


    }

    public void VerificaUDM(String barCode, StatusManager statusManager) {

    }

    public Boolean checkValidazioneUDM(){
        return WO.checkLavorabile();
    }
}
