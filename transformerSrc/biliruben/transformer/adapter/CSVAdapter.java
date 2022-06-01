package biliruben.transformer.adapter;

import java.util.Iterator;
import java.util.Map;

import com.biliruben.util.csv.CSVSource;
import com.biliruben.util.csv.CSVSource.CSVType;
import com.biliruben.util.csv.CSVSourceImpl;

public class CSVAdapter extends AbstractTransformerSourceAdapter {

    public static final String MIME_TYPE = "text/csv";
    /**
     * property group for csv source configuration properties
     */
    public static final String CSV_SOURCE = "csvSource";

    private CSVType csvType;
    private char delim;

    public CSVAdapter() {
        // required
        this.csvType = CSVType.WithHeader; // default
        this.delim = CSVSource.DEFAULT_DELIM;
    }

    /**
     * Future enhancement will be to allow configuration to define flattened multi-value columns
     * @ignore In theory, I could specify a configuration that defines a CSV source from a completely off-the-wall source
     */
    @Override
    public void configure(Map<String, Object> properties) {
        // use 'csvSource' properties to configure the adapter
        Object cfgObj = properties.get(CSV_SOURCE);
        if (cfgObj != null && cfgObj instanceof Map) {
            Map<String, Object> cfg = (Map<String, Object>) cfgObj;
            String csvType = (String) cfg.get("csvType");
            if (csvType != null) {
                this.csvType = CSVType.valueOf(csvType);
            }
            String csvDelim = (String) cfg.get("delim");
            if (csvDelim != null && !"".equals(csvDelim.trim())) {
                if (csvDelim.length() > 1) {
                    throw new IllegalArgumentException(csvDelim + " is not a valid delimter (must be one character)");
                }
                this.delim = csvDelim.charAt(0);
            }
        }
    }

    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    @Override
    public Iterator<Map<String, String>> iterator() {
        Iterator<Map<String, String>> it = null;
        CSVSource src = new CSVSourceImpl(this.getReader(), this.csvType);
        src.setDelim(this.delim);
        it = src.iterator();
        return it;
    }
}
