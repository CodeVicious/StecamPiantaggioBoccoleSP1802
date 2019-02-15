package sample;

import com.univocity.parsers.common.processor.BeanListProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class CsvParserService {
    private static CsvParserService ourInstance = new CsvParserService();
    public static CsvParserService getInstance() {
        return ourInstance;
    }

    final CsvParserSettings parserSettings;

    private CsvParserService() {
        parserSettings = new CsvParserSettings();
        // You can configure the parser to automatically detect what line
        // separator sequence is in the input
        parserSettings.setLineSeparatorDetectionEnabled(true);
        parserSettings.setHeaderExtractionEnabled(true);

        parserSettings.getFormat().setDelimiter(',');
        // BeanListProcessor converts each parsed row to an instance of a given
        // class, then stores each instance into a list.
        BeanListProcessor<RigaWO> rowProcessor = new BeanListProcessor<RigaWO>(RigaWO.class);

        // You can configure the parser to use a RowProcessor to process the
        // values of each parsed row.
        // You will find more RowProcessors in the
        // 'com.univocity.parsers.common.processor' package, but you can also
        // create your own.

        // You can configure the parser to use a RowProcessor to process the
        // values of each parsed row.
        // You will find more RowProcessors in the
        // 'com.univocity.parsers.common.processor' package, but you can also
        // create your own.
        parserSettings.setProcessor(rowProcessor);

        // Let's consider the first parsed row as the headers of each column in
        // the file.
        parserSettings.setHeaderExtractionEnabled(true);

        // creates a parser instance with the given settings
        CsvParser parser = new CsvParser(parserSettings);

        // the 'parse' method will parse the file and delegate each parsed row
        // to the RowProcessor you defined


        try {
            parser.parse(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("resources/WO.csv"), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        List<RigaWO> beans = rowProcessor.getBeans();
    }
}
