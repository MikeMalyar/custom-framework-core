package com.mmm.custom.framework.core.configuration.components.factory;

import com.mmm.custom.framework.core.configuration.annotations.Component;
import com.mmm.custom.framework.core.configuration.annotations.EnableComponentPostProcessing;
import com.mmm.custom.framework.core.configuration.annotations.dependency.InjectComponents;
import com.mmm.custom.framework.core.configuration.components.ComponentBean;
import com.mmm.custom.framework.core.configuration.post.processors.ComponentPostProcessor;
import com.mmm.custom.framework.core.exception.CircularDependencyException;
import com.mmm.custom.framework.core.exception.ComponentInitializationException;
import com.mmm.custom.framework.core.exception.ComponentPostProcessException;
import com.mmm.custom.framework.core.reflection.ReflectionAPIUtils;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Collectors;

import static com.mmm.custom.framework.core.utils.LoggingUtils.*;

public class ComponentFactory {

    private static final String DEFAULT_PACKAGE_TO_SCAN = "com.mmm.custom.framework.core";

    private Map<Class<?>, ComponentBean> componentBeans;

    public ComponentFactory() {
        componentBeans = new HashMap<>();
    }

    public void init(String ...rootPackageNames) {
        initializeAllComponents(rootPackageNames);

        performPostProcessForAllComponentBeans();
    }

    public ComponentBean getComponentBeanByClass(Class<?> clazz) {
        return componentBeans.get(clazz);
    }

    public Object getComponentByClass(Class<?> clazz) {
        ComponentBean componentBean = getComponentBeanByClass(clazz);
        return componentBean == null ? null : componentBean.getInstance();
    }

    public List<ComponentBean> getComponentBeansMarkedWithAnnotation(
            Class<? extends java.lang.annotation.Annotation> annotationClass) {
        return componentBeans.entrySet()
                .stream()
                .filter(classComponentBeanEntry -> ReflectionAPIUtils
                        .hasAnnotation(classComponentBeanEntry.getKey(), annotationClass))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    private void initializeAllComponents(String ...rootPackageNames) {
        log("Started components initialization");

        List<String> packagesToScan = new ArrayList<>();
        packagesToScan.add(DEFAULT_PACKAGE_TO_SCAN);
        packagesToScan.addAll(Arrays.asList(rootPackageNames));

        List<Class<?>> allClasses = new ArrayList<>();
        for (String rootPackageName: packagesToScan) {
            allClasses.addAll(ReflectionAPIUtils.fetchClassesFromPackageMarkedWithAnnotation(rootPackageName,
                    Component.class));
        }
        try {
            Map<Integer, List<Class<?>>> dependencyTree = buildDependencyTree(allClasses);

            log("Dependency tree >> ");
            for (int i = 0; i < dependencyTree.size(); ++i) {
                log(dependencyTree.get(i));

                for (Class<?> clazz : dependencyTree.get(i)) {
                    String componentId = clazz.getAnnotation(Component.class).id();
                    ComponentBean componentBean = new ComponentBean(clazz, componentId);
                    List<Constructor> constructors = ReflectionAPIUtils
                            .getConstructorsMarkedWithAnnotation(clazz, InjectComponents.class);
                    if (constructors.size() == 0) {
                        Object instance = ReflectionAPIUtils.initializeObjectByClass(clazz);
                        if (instance != null) {
                            componentBean.setInstance(instance);
                        } else {
                            throw new ComponentInitializationException(
                                    String.format("Component %s has no default constructor so cannot be instantiated",
                                            clazz.getName()));
                        }
                    } else {
                        List<Class<?>> parameters = Arrays.asList(constructors.get(0).getParameterTypes());
                        List<Object> values = parameters.stream()
                                .map(parameter -> componentBeans.get(parameter).getInstance())
                                .collect(Collectors.toList());
                        Object instance = ReflectionAPIUtils.initializeObjectByClass(clazz, parameters, values);
                        if (instance != null) {
                            componentBean.setInstance(instance);
                        } else {
                            throw new ComponentInitializationException(
                                    String.format("Component %s cannot be instantiated with provided constructor",
                                            clazz.getName()));
                        }
                    }
                    componentBeans.put(clazz, componentBean);
                }
            }
        } catch (ComponentInitializationException e) {
            errorLog(e);
        }

        log("Components created >>", stringifyCollectionForOutput(componentBeans));
    }

    private Map<Integer, List<Class<?>>> buildDependencyTree(List<Class<?>> inputClasses)
            throws ComponentInitializationException {
        Map<Integer, List<Class<?>>> dependencyTree = new HashMap<>();
        List<Class<?>> classes = new ArrayList<>(inputClasses);
        List<Class<?>> classesToRemove = new ArrayList<>();

        for (Class<?> clazz : classes) {
            List<Constructor> constructors = ReflectionAPIUtils
                    .getConstructorsMarkedWithAnnotation(clazz, InjectComponents.class);
            if (constructors.size() > 1) {
                throw new ComponentInitializationException(
                        String.format("Component %s has more than one injectable constructor", clazz.getName()));
            } else if (constructors.size() == 0) {
                classesToRemove.add(clazz);
            } else {
                List<Class<?>> parameters = Arrays.asList(constructors.get(0).getParameterTypes());
                if (!classes.containsAll(parameters)) {
                    throw new ComponentInitializationException(
                            String.format("Component %s constructor contains parameters which are not components",
                                    clazz.getName()));
                }
            }
        }
        dependencyTree.computeIfAbsent(0, k -> new ArrayList<>());
        dependencyTree.get(0).addAll(classesToRemove);
        classes.removeAll(classesToRemove);

        int level = 1;
        do {
            classesToRemove.clear();
            int finalLevel = level;
            for (Class<?> clazz : classes) {
                List<Constructor> constructors = ReflectionAPIUtils
                        .getConstructorsMarkedWithAnnotation(clazz, InjectComponents.class);
                List<Class<?>> parameters = Arrays.asList(constructors.get(0).getParameterTypes());
                if (dependencyTree.entrySet()
                        .stream()
                        .filter(dependency -> dependency.getKey() < finalLevel)
                        .map(Map.Entry::getValue)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList())
                        .containsAll(parameters)) {
                    classesToRemove.add(clazz);
                }
            }
            if (classesToRemove.size() > 0) {
                dependencyTree.computeIfAbsent(level, k -> new ArrayList<>());
                dependencyTree.get(level).addAll(classesToRemove);
                classes.removeAll(classesToRemove);
            } else {
                throw new CircularDependencyException(String
                        .format("Cannot initialize components due to circular reference in one of the classes: %s",
                                classes));
            }
            ++level;
        } while (classes.size() > 0);

        return dependencyTree;
    }

    private void performPostProcessForAllComponentBeans() {
        log("Started post-processing for all component beans");
        for (ComponentBean componentPostProcessorBean :
                getComponentBeansMarkedWithAnnotation(EnableComponentPostProcessing.class)) {
            ComponentPostProcessor componentPostProcessor
                    = (ComponentPostProcessor) componentPostProcessorBean.getInstance();
            for (ComponentBean componentBean : componentBeans.values()) {
                Object component = componentBean.getInstance();
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
}
