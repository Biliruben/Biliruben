package biliruben.transformer.adapter;

import java.util.Iterator;
import java.util.Map;

public class JsonSourceAdapter extends AbstractTransformerSourceAdapter {
    public static final String MIME_TYPE = "text/json";

    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    @Override
    public Iterator<Map<String, String>> iterator() {
        // TODO Auto-generated method stub
        return null;
    }

}
