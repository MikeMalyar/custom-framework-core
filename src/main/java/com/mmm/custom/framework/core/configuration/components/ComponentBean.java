package com.mmm.custom.framework.core.configuration.components;

import com.mmm.custom.framework.core.configuration.annotations.component.Component;
import com.mmm.custom.framework.core.configuration.annotations.component.ComponentStrategy;
import com.mmm.custom.framework.core.reflection.ReflectionAPIUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ComponentBean {

    private String id;

    private Class<?> componentClass;

    private Constructor constructorToInitialize;
    private Object[] constructorValues;

    private Map<Field, Object> fieldsToInitialize;

    private @Component Object instance;

    public ComponentBean(Class<?> componentClass) {
        this(componentClass, null, null, null);
    }

    public ComponentBean(Class<?> componentClass, String id) {
        this(componentClass, id, null, null, null);
    }

    public ComponentBean(Class<?> componentClass, Constructor constructorToInitialize, Object[] constructorValues) {
        this(componentClass, null, constructorToInitialize, constructorValues, null);
    }

    public ComponentBean(Class<?> componentClass, String id, Constructor constructorToInitialize,
                         Object[] constructorValues) {

        this(componentClass, id, constructorToInitialize, constructorValues, null);
    }

    public ComponentBean(Class<?> componentClass, String id, Constructor constructorToInitialize,
                         Object[] constructorValues, Map<Field, Object> fieldsToInitialize) {
        this.componentClass = componentClass;
        if (StringUtils.isNotBlank(id)) {
            this.id = id;
        } else {
            this.id = String.format("%sComponentBean", componentClass.getName());
        }
        this.constructorToInitialize = constructorToInitialize;
        this.fieldsToInitialize = Objects.requireNonNullElseGet(fieldsToInitialize, HashMap::new);
        this.constructorValues = constructorValues;
    }

    @Override
    public String toString() {
        return "ComponentBean{" +
                "id='" + id + '\'' +
                "; componentClass=" + componentClass +
                '}';
    }

    public String getId() {
        return id;
    }

    public Class<?> getComponentClass() {
        return componentClass;
    }

    public ComponentBean setComponentClass(Class<?> componentClass) {
        this.componentClass = componentClass;
        return this;
    }

    public Constructor getConstructorToInitialize() {
        return constructorToInitialize;
    }

    public ComponentBean setConstructorToInitialize(Constructor constructorToInitialize) {
        this.constructorToInitialize = constructorToInitialize;
        return this;
    }

    public Map<Field, Object> getFieldsToInitialize() {
        return fieldsToInitialize;
    }

    public ComponentBean setFieldsToInitialize(Map<Field, Object> fieldsToInitialize) {
        this.fieldsToInitialize = fieldsToInitialize;
        return this;
    }

    public @Component Object getInstance() {
        if (ComponentStrategy.FACTORY.equals(componentClass.getAnnotation(Component.class).strategy())) {
            return ReflectionAPIUtils.initializeObjectByClass(componentClass,
                    Arrays.asList(constructorToInitialize.getParameterTypes()),
                    Arrays.asList(constructorValues));
        }
        return instance;
    }

    public ComponentBean setInstance(@Component Object instance) {
        this.instance = instance;
        return this;
    }

    public Object[] getConstructorValues() {
        return constructorValues;
    }

    public ComponentBean setConstructorValues(Object[] constructorValues) {
        this.constructorValues = constructorValues;
        return this;
    }
}
