package com.mmm.custom.framework.core.property;

import com.mmm.custom.framework.core.configuration.annotations.Component;

@Component
public class ApplicationProperties {

    @PropertyValue(propertyName = "application.name")
    private String applicationName;
}
