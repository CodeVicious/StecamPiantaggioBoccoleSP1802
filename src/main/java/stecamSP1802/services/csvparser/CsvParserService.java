package stecamSP1802.services.csvparser;

import com.google.common.base.Preconditions;
import com.univocity.parsers.common.processor.BeanListProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import stecamSP1802.services.barcode.WorkOrder;

import java.io.*;
import java.util.List;

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

    synchronized public void parse(BufferedReader inputStream) {
        parser.parse(inputStream);
        beans = rowProcessor.getBeans();
    }

    public void fillWO() {
        Preconditions.checkNotNull(beans);
        WorkOrder wo = WorkOrder.getInstance();
        wo.setBarCodeWO(beans.get(0).getWo());
        wo.setCodiceRicetta(beans.get(0).getArticolo());
        wo.setDescrizione(beans.get(0).getDescrizione());
        for (int i = 1; i < beans.size(); i++) {
            wo.addParte("", beans.get(i).getArticolo(), beans.get(i).getDescrizione(), false);
        }
    }
}
