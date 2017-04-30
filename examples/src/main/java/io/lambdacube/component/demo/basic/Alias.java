package io.lambdacube.component.demo.basic;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import io.primeval.component.annotation.properties.ComponentProperty;

@ComponentProperty("custom.alias")
@Target(ElementType.TYPE)
public @interface Alias {
    String value();
}
