package io.lambdacube.component.demo.gogo.property;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import io.primeval.component.annotation.properties.ComponentProperty;
import io.primeval.component.annotation.properties.ComponentPropertyGroup;
import io.primeval.component.annotation.properties.EnsureProvideService;

@EnsureProvideService
@ComponentPropertyGroup
@Target(ElementType.TYPE)
public @interface GogoCommand {

    @ComponentProperty("osgi.command.scope")
    String scope();

    @ComponentProperty("osgi.command.function")
    String[]commandFunction();
}