package stecamSP1802.services.csvparser;

import com.google.common.base.Preconditions;
import com.univocity.parsers.common.processor.BeanListProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import stecamSP1802.services.barcode.WorkOrder;
import sun.rmi.runtime.Log;

import java.io.*;
import java.util.List;

import static sun.java2d.cmm.ColorTransform.In;

public class CsvParserService {
    final static Logger Logger = LogManager.getLogger(CsvParserService.class);
    final CsvParserSettings parserSettings;
    CsvParser parser;
    BeanListProcessor<RigaWO> rowProcessor;
    private BufferedReader inputStreamReader;
    List<RigaWO> beans;

    public CsvParserService() {
        parserSettings = new CsvParserSettings();
        // You can configure the parser to automatically detect what line
        // separator sequence is in the input
        parserSettings.setLineSeparatorDetectionEnabled(true);
        parserSettings.setHeaderExtractionEnabled(true);

        parserSettings.getFormat().setDelimiter(';');
        // BeanListProcessor converts each parsed row to an instance of a given
        // class, then stores each instance into a list.
        rowProcessor = new BeanListProcessor<RigaWO>(RigaWO.class);

        parserSettings.setProcessor(rowProcessor);

        parserSettings.setHeaderExtractionEnabled(true);

        // creates a parser instance with the given settings
        parser = new CsvParser(parserSettings);

        // the 'parse' method will parse the file and delegate each parsed row
        // to the RowProcessor you defined
    }

    synchronized public String parse(InputStream inputStream){

        this.inputStreamReader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            String line =this.inputStreamReader.readLine();
            if(!line.matches("OK")){
                Logger.error("MISSING OK AS FIRST LINE - FIRST LINE "+line+"");
            }
            else{
                Logger.info("SKIPPING OK AND START PARSING");
                parser.parse(inputStreamReader);
                beans = rowProcessor.getBeans();
            }
            return line;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public WorkOrder getWO() {
        Preconditions.checkNotNull(beans);
        WorkOrder wo = new WorkOrder(beans.get(0).getWo(),beans.get(0).getArticolo());
        for(int i = 1;i < beans.size(); i++){
            wo.addParte(beans.get(i).getArticolo(),beans.get(i).getDescrizione(),false);
        }
        return wo;
    }
}
