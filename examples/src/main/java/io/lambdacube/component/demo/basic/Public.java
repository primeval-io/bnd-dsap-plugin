package io.lambdacube.component.demo.basic;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import io.lambdacube.component.annotation.ComponentProperty;

@ComponentProperty(CustomProperties.PUBLIC)
@Target(ElementType.TYPE)
public @interface Public {
}
