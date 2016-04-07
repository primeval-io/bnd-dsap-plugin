package io.lambdacube.component.demo.gogo.property;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import io.lambdacube.component.annotation.ComponentProperty;
import io.lambdacube.component.annotation.ComponentPropertyGroup;
import io.lambdacube.component.annotation.EnsureProvideService;

@EnsureProvideService
@ComponentPropertyGroup
@Target(ElementType.TYPE)
public @interface GogoCommand {

    @ComponentProperty("osgi.command.scope")
    String scope();

    @ComponentProperty("osgi.command.function")
    String[]commandFunction();
}