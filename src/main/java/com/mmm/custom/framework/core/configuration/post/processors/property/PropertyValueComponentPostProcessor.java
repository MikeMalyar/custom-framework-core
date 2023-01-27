package com.mmm.custom.framework.core.configuration.post.processors.property;

import com.mmm.custom.framework.core.configuration.annotations.Component;
import com.mmm.custom.framework.core.configuration.annotations.EnableComponentPostProcessing;
import com.mmm.custom.framework.core.configuration.post.processors.ComponentPostProcessor;
import com.mmm.custom.framework.core.exception.PropertyNotAccessibleException;
import com.mmm.custom.framework.core.property.PropertyValue;
import com.mmm.custom.framework.core.reflection.ReflectionAPIUtils;

@Component
@EnableComponentPostProcessing
public class PropertyValueComponentPostProcessor implements ComponentPostProcessor {

    @Override
    public boolean postProcessComponent(@Component Object component) throws PropertyNotAccessibleException {
        Class<?> clazz = component.getClass();
        try {
            return ReflectionAPIUtils.updateObjectFieldMarkedWithAnnotation(component, PropertyValue.class,
                    field -> field.getAnnotation(PropertyValue.class).propertyName());  //TODO change to get value of application property
        } catch (IllegalAccessException e) {
            throw new PropertyNotAccessibleException(
                    String.format("Failed to set property for component %s", clazz.getName()), e);
        }
    }
}
