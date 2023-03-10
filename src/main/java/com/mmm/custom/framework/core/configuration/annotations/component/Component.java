package com.mmm.custom.framework.core.configuration.annotations.component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Component {

    String id() default "";

    ComponentStrategy strategy() default ComponentStrategy.SINGLETON;
}
