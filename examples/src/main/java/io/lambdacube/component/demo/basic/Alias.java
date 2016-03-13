package io.lambdacube.component.demo.basic;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import io.lambdacube.component.annotation.ComponentProperty;

@ComponentProperty("custom.alias")
@Target(ElementType.TYPE)
public @interface Alias {
    String value();
}
