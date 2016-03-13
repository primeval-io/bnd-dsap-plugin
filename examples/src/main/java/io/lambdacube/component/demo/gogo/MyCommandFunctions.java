package io.lambdacube.component.demo.gogo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import io.lambdacube.component.annotation.ComponentProperty;

@ComponentProperty("osgi.command.function")
@Target(ElementType.TYPE)
public @interface MyCommandFunctions {
    MyFunctions[] value();
}