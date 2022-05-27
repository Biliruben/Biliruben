package biliruben.transformer.adapter;

import java.io.Reader;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import biliruben.transformer.MappedMimeType;
import biliruben.transformer.Util;

public abstract class AbstractTransformerSourceAdapter implements TransformerSourceAdapter, MappedMimeType {

    private static Log log = LogFactory.getLog(AbstractTransformerSourceAdapter.class);

    private Reader sourceReader;

    @Override
    public void setReader(Reader source) {
        log.trace("setReader: source = " + source);
        this.sourceReader = source;
    }

    public Reader getReader() {
        log.trace("getReader: " + this.sourceReader);
        return this.sourceReader;
    }

    public static Class<? extends AbstractTransformerSourceAdapter> getTransformer(String forMimeType) {
        log.trace("getTransformer: forMimeType = " + forMimeType);
        Class<? extends MappedMimeType> foundClass = Util.getClassForMimeType(forMimeType, AbstractTransformerSourceAdapter.class);
        Class<? extends AbstractTransformerSourceAdapter> foundAdapterClass = null;
        if (foundClass != null && AbstractTransformerSourceAdapter.class.isAssignableFrom(foundClass)) {
            foundAdapterClass = (Class<? extends AbstractTransformerSourceAdapter>) foundClass;
        }
        log.trace("Returning: " + foundClass);
        return foundAdapterClass;
    }

    public static void main(String[] args) {
        // Default properties
        Properties props = new Properties();
        props.setProperty("log4j.appender.stdout","org.apache.log4j.ConsoleAppender");
        props.setProperty("log4j.appender.stdout.Target","System.out");
        props.setProperty("log4j.appender.stdout.layout","org.apache.log4j.PatternLayout");
        props.setProperty("log4j.appender.stdout.layout.ConversionPattern","%d{ISO8601} %5p %t %c{4}:%L - %m%n");
        props.setProperty("log4j.rootLogger", "debug" + ",stdout");

        PropertyConfigurator.configure(props);
        System.out.println(getTransformer("json"));
    }
}
