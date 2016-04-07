package io.lambdacube.component.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
public @interface ComponentProperty {

    // Property name
    String value();
}
