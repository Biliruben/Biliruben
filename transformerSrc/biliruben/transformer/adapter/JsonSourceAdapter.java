package biliruben.transformer.adapter;

import java.util.Iterator;
import java.util.Map;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

public class JsonSourceAdapter extends AbstractTransformerSourceAdapter {
    public static final String MIME_TYPE = "text/json";

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
        // TODO Auto-generated method stub
        return null;
    }

}
