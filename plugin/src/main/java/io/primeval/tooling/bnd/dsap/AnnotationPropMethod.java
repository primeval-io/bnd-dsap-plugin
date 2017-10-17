package io.primeval.tooling.bnd.dsap;

public final class AnnotationPropMethod {
    public final String propName;
    public final String typeName;
    public final boolean isArray;
    public final String annotationMethodName;
	public Object defaultValue;

    public AnnotationPropMethod(String propName, String typeName, boolean isArray, String annotationMethodName) {
        super();
        this.propName = propName;
        this.typeName = typeName;
        this.isArray = isArray;
        this.annotationMethodName = annotationMethodName;
    }

    public void setDefaultValue(Object defaultValue)
	{
        this.defaultValue = defaultValue;
	}
}
