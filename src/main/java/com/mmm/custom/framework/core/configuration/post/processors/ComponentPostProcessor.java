package com.mmm.custom.framework.core.configuration.post.processors;

import com.mmm.custom.framework.core.configuration.annotations.component.Component;
import com.mmm.custom.framework.core.exception.ComponentPostProcessException;

public interface ComponentPostProcessor {

    boolean postProcessComponent(@Component Object component) throws ComponentPostProcessException;
}
