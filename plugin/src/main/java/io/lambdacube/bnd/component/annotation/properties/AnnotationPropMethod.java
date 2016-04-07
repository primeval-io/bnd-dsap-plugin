package io.lambdacube.bnd.component.annotation.properties;

public final class AnnotationPropMethod {
    public final String propName;
    public final String typeName;
    public final boolean isArray;
    public final String annotationMethodName;

    public AnnotationPropMethod(String propName, String typeName, boolean isArray, String annotationMethodName) {
        super();
        this.propName = propName;
        this.typeName = typeName;
        this.isArray = isArray;
        this.annotationMethodName = annotationMethodName;
    }

}
