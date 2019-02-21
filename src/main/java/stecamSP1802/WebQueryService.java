package stecamSP1802;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import stecamSP1802.services.csvparser.CsvParserService;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class WebQueryService {
    //final URL url = new URL(ConfigurationManager.getInstance().getVerificaListaPartiWOURL());

    final static Logger Logger = LogManager.getLogger(WebQueryService.class);
    private CsvParserService csvParserService;
    private InputStream iFile;

    public WebQueryService(){
        csvParserService = new CsvParserService();

    }

    synchronized public void VerificaListaPartiWO(String barCode)  {

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



        csvParserService.parse(iFile);
        Logger.info("SERVICE ");
        //BufferedReader br = new BufferedReader(new InputStreamReader(iFile));

        String line = null;
/*
        // read each line and write to System.out
        while ((line = br.readLine()) != null) {
            if(line=="OK")
                continue;

            System.out.println(line);
        }
        */
    }
}
