package biliruben.transformer.adapter;

import java.io.Reader;
import java.util.Map;

import biliruben.transformer.Configurable;

public interface TransformerSourceAdapter extends Iterable<Map<String, String>>, Configurable {

    void setReader(Reader reader);
}
