package com.mmm.custom.framework.core.configuration.context;

import com.mmm.custom.framework.core.configuration.annotations.Component;
import com.mmm.custom.framework.core.reflection.ReflectionAPIUtils;

import java.util.*;

public class AppContext {

    private static final String DEFAULT_PACKAGE_TO_SCAN = "com.mmm.custom.framework.core";

    private Map<Class<?>, Object> components;

    public AppContext(String ...rootPackageNames) {
        init(rootPackageNames);
    }

    private void init(String ...rootPackageNames) {
        log("Started AppContext initialization");

        components = new HashMap<>();

        initializeAllComponents(rootPackageNames);

        log("Components created >>", components);
    }

    private void initializeAllComponents(String ...rootPackageNames) {
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
    }

    private <T> void log(T ...objectsToLog) {
        for (T objectToLog : objectsToLog) {
            System.out.println(objectToLog);
        }
    }
}
