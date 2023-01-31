package com.mmm.custom.framework.core.configuration.context;

import com.mmm.custom.framework.core.configuration.components.factory.ComponentFactory;

import static com.mmm.custom.framework.core.utils.LoggingUtils.log;

public class AppContext {

    private ComponentFactory componentFactory;

    public AppContext(String ...rootPackageNames) {
        init(rootPackageNames);
    }

    public Object getComponentByClass(Class<?> clazz) {
        return componentFactory.getComponentByClass(clazz);
    }

    private void init(String ...rootPackageNames) {
        log("Started AppContext initialization");

        componentFactory = new ComponentFactory();

        componentFactory.init(rootPackageNames);
    }
}
