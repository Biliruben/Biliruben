package biliruben.transformer.adapter;

import java.io.Reader;
import java.util.Iterator;
import java.util.Map;

import biliruben.transformer.TransformException;

public interface TransformerSourceAdapter extends Iterable<Map<String, String>> {

    void setReader(Reader reader);
}
