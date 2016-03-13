package io.lambdacube.component.demo.gogo.property;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import io.lambdacube.component.annotation.ComponentProperty;

@ComponentProperty("osgi.command.scope")
@Target(ElementType.TYPE)
public @interface CommandScope {
    String value();
}