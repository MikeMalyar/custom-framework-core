package com.mmm.custom.framework.core.utils;

import java.util.Collection;
import java.util.Map;

public final class LoggingUtils {

    private LoggingUtils() {}

    public static <T> void log(T ...objectsToLog) {
        for (T objectToLog : objectsToLog) {
            System.out.println(objectToLog);
        }
    }

    public static <T> void errorLog(T ...objectsToLog) {
        for (T objectToLog : objectsToLog) {
            log("ERROR >>", objectToLog);
        }
    }

    public static <K, V> String stringifyCollectionForOutput(Map<K, V> collection) {
        return collection.toString().replaceAll(",", ",\n");
    }

    public static <T> String stringifyCollectionForOutput(Collection<T> collection) {
        return collection.toString().replaceAll(",", ",\n");
    }
}
