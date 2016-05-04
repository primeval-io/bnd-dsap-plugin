package io.lambdacube.component.annotation.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import org.osgi.framework.Constants;

import io.lambdacube.component.annotation.ComponentProperty;

@ComponentProperty(Constants.SERVICE_RANKING)
@Target(ElementType.TYPE)
public @interface ServiceRanking {
    int value();
}
