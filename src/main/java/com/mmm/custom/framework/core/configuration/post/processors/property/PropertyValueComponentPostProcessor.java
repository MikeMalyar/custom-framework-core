package com.mmm.custom.framework.core.configuration.post.processors.property;

import com.mmm.custom.framework.core.configuration.annotations.Component;
import com.mmm.custom.framework.core.configuration.annotations.EnableComponentPostProcessing;
import com.mmm.custom.framework.core.configuration.post.processors.ComponentPostProcessor;
import com.mmm.custom.framework.core.exception.PropertyNotAccessibleException;
import com.mmm.custom.framework.core.property.BaseApplicationProperties;
import com.mmm.custom.framework.core.property.PropertyValue;
import com.mmm.custom.framework.core.reflection.ReflectionAPIUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Properties;

@Component
@EnableComponentPostProcessing
public class PropertyValueComponentPostProcessor implements ComponentPostProcessor {

    @Override
    public boolean postProcessComponent(@Component Object component) throws PropertyNotAccessibleException {
        Class<?> clazz = component.getClass();
        try {
            return ReflectionAPIUtils.updateObjectFieldsMarkedWithAnnotation(component, PropertyValue.class,
                    this::setFieldPropertyValue);
        } catch (IllegalAccessException | IllegalStateException e) {
            throw new PropertyNotAccessibleException(
                    String.format("Failed to set property for component %s", clazz.getName()), e);
        }
    }

    private Object setFieldPropertyValue(Field field) {
        PropertyValue annotation = field.getAnnotation(PropertyValue.class);
        String propertyName = annotation.propertyName();
        String defaultValue = annotation.defaultValue();
        try {
            Properties properties = BaseApplicationProperties.loadProperties("application.properties");
            return properties.getOrDefault(propertyName, defaultValue);
        } catch (IOException | NullPointerException e) {
            throw new IllegalStateException(e);
        }
    }
}
