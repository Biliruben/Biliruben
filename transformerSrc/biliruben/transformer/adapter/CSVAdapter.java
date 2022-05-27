package biliruben.transformer.adapter;

import java.util.Iterator;
import java.util.Map;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import com.biliruben.util.csv.CSVSource;
import com.biliruben.util.csv.CSVSource.CSVType;
import com.biliruben.util.csv.CSVSourceImpl;

public class CSVAdapter extends AbstractTransformerSourceAdapter {

    private static final String MIME_TYPE = "text/csv";

    public CSVAdapter() {
        // required
    }

    @Override
    public MimeType getMimeType() {
        try {
            return new MimeType(MIME_TYPE);
        } catch (MimeTypeParseException e) {
            // This is a programming error; RTE it
            throw new RuntimeException(e);
        }
    }

    @Override
    public Iterator<Map<String, String>> iterator() {
        Iterator<Map<String, String>> it = null;
        CSVSource src = new CSVSourceImpl(this.getReader(), CSVType.WithHeader);
        it = src.iterator();
        return it;
    }
}
