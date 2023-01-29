package com.mmm.custom.framework.core.property;

import com.mmm.custom.framework.core.configuration.annotations.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Component
public class BaseApplicationProperties {

    @PropertyValue(propertyName = "application.name")
    private String applicationName;

    public String getApplicationName() {
        return applicationName;
    }

    public static Properties loadProperties(String fileName) throws IOException {
        Properties properties = new Properties();
        InputStream inputStream = BaseApplicationProperties.class
                .getClassLoader()
                .getResourceAsStream(fileName);
        assert inputStream != null;
        properties.load(inputStream);
        inputStream.close();
        return properties;
    }
}
