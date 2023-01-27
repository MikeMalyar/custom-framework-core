package com.mmm.custom.framework.core.configuration.context;

import com.mmm.custom.framework.core.configuration.annotations.Component;
import com.mmm.custom.framework.core.configuration.annotations.EnableComponentPostProcessing;
import com.mmm.custom.framework.core.configuration.post.processors.ComponentPostProcessor;
import com.mmm.custom.framework.core.exception.ComponentPostProcessException;
import com.mmm.custom.framework.core.reflection.ReflectionAPIUtils;

import java.util.*;
import java.util.stream.Collectors;

public class AppContext {

    private static final String DEFAULT_PACKAGE_TO_SCAN = "com.mmm.custom.framework.core";

    private Map<Class<?>, Object> components;
    private Map<Class<?>, Object> componentPostProcessors;

    public AppContext(String ...rootPackageNames) {
        init(rootPackageNames);
    }

    public Object getComponentByClass(Class<?> clazz) {
        return components.get(clazz);
    }

    private void init(String ...rootPackageNames) {
        log("Started AppContext initialization");

        components = new HashMap<>();
        componentPostProcessors = new HashMap<>();

        initializeAllComponents(rootPackageNames);

        processAllPostProcessors();

        performPostProcess();
    }

    private void initializeAllComponents(String ...rootPackageNames) {
        log("Started components initialization");
        List<String> packagesToScan = new ArrayList<>();
        packagesToScan.add(DEFAULT_PACKAGE_TO_SCAN);
        packagesToScan.addAll(Arrays.asList(rootPackageNames));
        for (String rootPackageName: packagesToScan) {
            List<Class<?>> classes = ReflectionAPIUtils.fetchClassesFromPackageMarkedWithAnnotation(rootPackageName,
                    Component.class);
            for (Class<?> clazz : classes) {
                Object instance = ReflectionAPIUtils.initializeObjectByClass(clazz); //TODO need to choose constructor here
                if (instance != null) {
                    components.put(clazz, instance);
                }
            }
        }
        log("Components created >>", stringifyCollectionForOutput(components));
    }

    private void processAllPostProcessors() {
        log("Started post-processor processing");
        componentPostProcessors.putAll(components.entrySet()
                .stream()
                .filter(classObjectEntry -> ReflectionAPIUtils
                        .hasAnnotation(classObjectEntry.getKey(), EnableComponentPostProcessing.class))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        log("Post processors processed >>", stringifyCollectionForOutput(componentPostProcessors));
    }

    private void performPostProcess() {
        log("Started post-processing");
        for (Map.Entry<Class<?>, Object> componentPostProcessorEntry : componentPostProcessors.entrySet()) {
            ComponentPostProcessor componentPostProcessor
                    = (ComponentPostProcessor) componentPostProcessorEntry.getValue();
            for (Map.Entry<Class<?>, Object> componentEntry : components.entrySet()) {
                Object component = componentEntry.getValue();
                try {
                    if (componentPostProcessor.postProcessComponent(component)) {
                        log("Post processed component " + component);
                    }
                } catch (ComponentPostProcessException e) {
                    errorLog(e);
                }
            }
        }
    }

    private <T> void log(T ...objectsToLog) {
        for (T objectToLog : objectsToLog) {
            System.out.println(objectToLog);
        }
    }

    private <T> void errorLog(T ...objectsToLog) {
        for (T objectToLog : objectsToLog) {
            log("ERROR >>", objectToLog);
        }
    }

    private <K, V> String stringifyCollectionForOutput(Map<K, V> collection) {
        return collection.toString().replaceAll(",", ",\n");
    }

    private <T> String stringifyCollectionForOutput(Collection<T> collection) {
        return collection.toString().replaceAll(",", ",\n");
    }
}
