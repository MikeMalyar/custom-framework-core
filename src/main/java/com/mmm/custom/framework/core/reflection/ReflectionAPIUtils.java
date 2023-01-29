package com.mmm.custom.framework.core.reflection;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class ReflectionAPIUtils {

    private ReflectionAPIUtils() {}

    public static List<Class<?>> fetchClassesFromPackage(String packageName) {

        InputStream inputStream = ClassLoader.
                getSystemResourceAsStream(packageName.replace(".", "/"));

        if (inputStream != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            return reader.lines()
                    .map(line -> {
                        if (line.endsWith(".class")) {
                            Class<?> clazz = getClassByName(line, packageName);
                            return clazz == null ? List.<Class<?>>of() : List.of(clazz);
                        } else {
                            return fetchClassesFromPackage(packageName + "." + line);
                        }
                    })
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public static List<Class<?>> fetchClassesFromPackageMarkedWithAnnotation(String packageName,
             Class<? extends java.lang.annotation.Annotation> annotationClass) {
        List<Class<?>> allClasses = fetchClassesFromPackage(packageName);
        return allClasses.stream()
                .filter(clazz -> hasAnnotation(clazz, annotationClass))
                .collect(Collectors.toList());
    }

    public static List<Class<?>> fetchClassesFromPackageHasAnnotatedFields(String packageName,
            Class<? extends java.lang.annotation.Annotation> annotationClass) {
        List<Class<?>> allClasses = fetchClassesFromPackage(packageName);
        return allClasses.stream()
                .filter(clazz -> Arrays.stream(clazz.getDeclaredFields())
                        .anyMatch(field -> field.isAnnotationPresent(annotationClass)))
                .collect(Collectors.toList());
    }

    public static <T> T initializeObjectByClass(Class<T> clazz) {
        try {
            Constructor<?> constructor = clazz.getConstructor();
            constructor.setAccessible(true);
            return (T) constructor.newInstance();
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException
                | IllegalAccessException e) {
            return null;
        }
    }

    public static <T> T initializeObjectByClass(Class<T> clazz, List<Class<?>> parameterTypes,
                                                List<Object> parameterValues) {
        try {
            Constructor<?> constructor = clazz.getConstructor(parameterTypes.toArray(new Class<?>[0]));
            constructor.setAccessible(true);
            return (T) constructor.newInstance(parameterValues.toArray());
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException
                | IllegalAccessException e) {
            return null;
        }
    }

    public static <T> boolean updateObjectFieldsMarkedWithAnnotation(Object object,
             Class<? extends java.lang.annotation.Annotation> annotationClass, Function<Field, T> valueFunction)
            throws IllegalAccessException {
        return updateObjectFieldsMarkedWithAnnotation(object, object.getClass(), annotationClass, valueFunction);
    }

    public static <T> boolean updateObjectFieldsMarkedWithAnnotation(Object object, Class<?> objectClass,
            Class<? extends java.lang.annotation.Annotation> annotationClass, Function<Field, T> valueFunction)
            throws IllegalAccessException {
        boolean hasUpdated = false;
        do {
            for (Field field : objectClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(annotationClass)) {
                    field.setAccessible(true);
                    field.set(object, valueFunction.apply(field));
                    hasUpdated = true;
                }
            }
        } while ((objectClass = objectClass.getSuperclass()) != Object.class);
        return hasUpdated;
    }

    public static boolean hasAnnotation(Class<?> clazz,
                                        Class<? extends java.lang.annotation.Annotation> annotationClass) {
        return clazz.isAnnotationPresent(annotationClass);
    }

    public static Class<?> getClassByName(String classFileName, String packageName) {
        try {
            return Class.forName(packageName + "." + classFileName.substring(0, classFileName.lastIndexOf(".")));
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
