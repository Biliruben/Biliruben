package biliruben.transformer;

import java.lang.reflect.Method;
import java.util.Set;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.reflections.Reflections;

public class Util {

    private static Log log = LogFactory.getLog(Util.class);
    
    public static Class<? extends MappedMimeType> getClassForMimeType(String forMimeType, Class<? extends MappedMimeType> baseClass) {
        log.trace("getClassForMimeType: baseClass = " + baseClass +"; forMimeType = " + forMimeType);
        // convert the string to a MimeType; blow up when it's not the right format
        MimeType mimeType = null;
        try {
            mimeType = new MimeType(forMimeType);
            log.debug("Using MimeType: " + mimeType);
        } catch (MimeTypeParseException mte) {
            // This is an exception, but one we expect. Log to INFO is fine
            log.info("Error parsing forMimeType: " + forMimeType, mte);
        }
        
        // Build a map of MimeTypes to Classes
            Reflections reflections = new Reflections(baseClass.getPackage().getName());
            Set transformerClasses = reflections.getSubTypesOf(baseClass);
            if (log.isTraceEnabled()) {
                log.trace("Found classes:");
                for (Object transformerClassObj : transformerClasses) {
                    log.trace(transformerClassObj);
                }
            }

        Class<? extends MappedMimeType> foundClass = null;
        for (Object classObj : transformerClasses) {
            if (log.isDebugEnabled()) log.debug("Testing " + classObj);
            try {
                // It's an assumption, but a really good one
                Class<MappedMimeType> clazz = (Class)classObj;
                // This is an instance method, so build an instance
                MappedMimeType instance = clazz.newInstance();
                Method m = clazz.getMethod("getMimeType", (Class[])null);
                Object ret = m.invoke(instance);
                if (log.isDebugEnabled()) log.debug("ret = " + ret);
                if (ret instanceof String) {
                    MimeType theirMimeType = null;
                    try {
                        theirMimeType = new MimeType((String)ret);
                    } catch (MimeTypeParseException e) {
                        log.error(e.getMessage(), e);
                        // leave theirMimeType as null
                    }
                    // if it's an exact match, we done
                    if (mimeType != null && theirMimeType.match(mimeType)) {
                        // winner!
                        log.trace("Returning match: " + clazz);
                        return clazz;
                    } else if (theirMimeType.getSubType().equalsIgnoreCase(forMimeType)) {
                        // If we have to rely on a subtype match, keep going in case there's a 
                        // better match down the line
                        log.debug("Found subtype match: " + clazz);
                        foundClass = clazz;
                    }
                }
            } catch (Exception nme) {
                // This is probably one we should log, but keep trying
                log.error(nme.getMessage(), nme);
            }
        }

        log.trace("Returning: " + foundClass);
        return foundClass;
    }

}
