package io.lambdacube.component.demo.gogo.property;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import io.lambdacube.component.annotation.ComponentProperty;
import io.lambdacube.component.annotation.EnsureProvideService;

@ComponentProperty("osgi.command.scope")
@EnsureProvideService
@Target(ElementType.TYPE)
public @interface CommandScope {
    String value();
}