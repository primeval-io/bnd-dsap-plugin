package io.primeval.tooling.bnd.dsap;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import aQute.bnd.component.TagResource;
import aQute.bnd.osgi.Analyzer;
import aQute.bnd.osgi.Annotation;
import aQute.bnd.osgi.ClassDataCollector;
import aQute.bnd.osgi.Clazz;
import aQute.bnd.osgi.Clazz.MethodDef;
import aQute.bnd.osgi.Constants;
import aQute.bnd.osgi.Resource;
import aQute.bnd.service.AnalyzerPlugin;
import aQute.lib.tag.Tag;
import io.primeval.component.annotation.properties.ComponentProperty;
import io.primeval.component.annotation.properties.ComponentPropertyGroup;
import io.primeval.component.annotation.properties.EnsureProvideService;

public final class DSAPPlugin implements AnalyzerPlugin {

	private static final Pattern propertyPattern = Pattern.compile("\\$\\{([a-zA-Z.-]+)\\}");

    @Override
    public boolean analyzeJar(Analyzer analyzer) throws Exception {
        String sc = analyzer.getProperty(Constants.SERVICE_COMPONENT);

        if (sc == null || sc.trim().isEmpty()) {
            return false;
        }

        String[] components = sc.split(",");
        Field tagField = TagResource.class.getDeclaredField("tag");
        tagField.setAccessible(true);
        for (String comp : components) {

            Resource resource = analyzer.getJar().getResource(comp);
            if (!(resource instanceof TagResource)) {
                analyzer.warning("Cannot process component %s because its resource is not a TagResource", comp);
                continue;
            }
            TagResource tagResource = (TagResource) resource;
            Tag tag = (Tag) tagField.get(tagResource);
            String className = tag.select("implementation").iterator().next().getAttribute("class");

            Clazz clazz = analyzer.getClassspace().values().stream().filter(n -> n.getClassName().getFQN().equals(className))
                    .findFirst().orElse(null);
            if (clazz == null) {
                analyzer.warning("Couldn't find class %s, skipping", className);
                continue;
            }
            boolean[] ensureService = new boolean[1];
            clazz.parseClassFileWithCollector(new ClassDataCollector() {

                @Override
                public void annotation(Annotation annotation) throws Exception {
                    Clazz annClazz = analyzer.findClass(annotation.getName());

                    String[] flatPropertyName = new String[1];
                    MethodDef[] lastMethod = new MethodDef[1];

                    boolean[] isComponentPropertyGroup = new boolean[1];

                    Map<String, String> propertyByMethod = new HashMap<>();

                    annClazz.parseClassFileWithCollector(new ClassDataCollector() {

                    	
                        @Override
                        public void method(MethodDef defined) {
                            lastMethod[0] = defined;
                        }

                        public void annotation(Annotation annotation) throws Exception {
                            switch (annotation.getElementType()) {
                            case TYPE:
                                if (ComponentPropertyGroup.class.getName().equals(annotation.getName().getFQN())) {
                                    isComponentPropertyGroup[0] = true;
                                } else if (ComponentProperty.class.getName().equals(annotation.getName().getFQN())) {
                                    String v = annotation.get("value");
                                    flatPropertyName[0] = v;
                                } else if (EnsureProvideService.class.getName().equals(annotation.getName().getFQN())) {
                                    ensureService[0] = true;
                                }
                                break;

                            case METHOD:
                                if (ComponentProperty.class.getName().equals(annotation.getName().getFQN())) {
                                    String v = annotation.get("value");
                                    propertyByMethod.put(lastMethod[0].getName(), v);
                                }
                                break;
                            default:
                                break;
                            }

                        };
                    });

                    String flatProp = flatPropertyName[0];

                    if (isComponentPropertyGroup[0] || flatProp != null) {

                        List<AnnotationPropMethod> props = new ArrayList<>();

                        // Read twice to be sure we only deal with the
                        // annotations we're interested in
                        annClazz.parseClassFileWithCollector(new ClassDataCollector() {
                        	@Override
                        	public void annotationDefault(MethodDef last)
                        	{
                        		if(props.size() < 1)
                        		{
                        			return;
                        		}

                        		AnnotationPropMethod apm = props.get(props.size()-1);
                        		Object value = last.getConstant();

                        		if(value instanceof String)
                        		{
                        			value = replaceProperties((String) value, analyzer.getProperties());
                        		}

								apm.setDefaultValue(value);
                        	}

                            public void method(MethodDef last) {
                                String prop = null;
                                if (flatProp != null) {
                                    if (!"value".equals(last.getName())) {
                                        analyzer.error(
                                                "Component property annotations can only have one field named value, found method named %s",
                                                last.getName());
                                        return;
                                    } else {
                                        prop = flatProp;
                                    }
                                } else if (isComponentPropertyGroup[0]) {
                                    prop = propertyByMethod.get(last.getName());
                                    if (prop == null) {
                                        // No prop, ignore
                                        return;
                                    }
                                }

                                String methodName = last.getName();

                                String type = last.getGenericReturnType();
                                boolean isArray = type.endsWith("[]");
                                String typeName = null;
                                if (isArray) {
                                    type = type.substring(0, type.length() - 2);
                                }
                                switch (type) {
                                case "int":
                                case "short":
                                    typeName = "Integer";
                                    break;
                                case "float":
                                    typeName = "Float";
                                    break;
                                case "double":
                                    typeName = "Double";
                                    break;
                                case "boolean":
                                    typeName = "Boolean";
                                    break;
                                default:
                                    typeName = "String";
                                    break;
                                }

                                AnnotationPropMethod apm = new AnnotationPropMethod(prop, typeName, isArray, methodName);

                                props.add(apm);
                            };

                        });

                        if (props.isEmpty()) {

                            if (flatProp != null) {
                                props.add(new AnnotationPropMethod(flatProp, "Boolean", false, null));
                            } else if (isComponentPropertyGroup[0]) {
                                analyzer.error("Annotations annotated with @ComponentPropertyGroup must not be empty, found %s",
                                        annClazz.getFQN());
                                return;
                            }
                        }

                        for (AnnotationPropMethod apm : props) {
                            Object value = null;
                            // No method.
                            if (apm.annotationMethodName == null) {
                                value = Boolean.TRUE;
                            } else {
                                value = annotation.get(apm.annotationMethodName);
                            }
                            if (value == null) {
                                if (apm.defaultValue == null && apm.isArray) {
                                    value = new Object[0];
                                } else {
                                	value = apm.defaultValue;
                                }
//                                else {
//                                    analyzer.error("Default values are supported only for arrays (default to empty), fix annotation %s",
//                                            annClazz.getFQN());
//                                    return;
//                                }
                            }

                            String outValue = null;
                            boolean array = false;
                            if (value.getClass().isArray()) {
                                Object[] arrVal = (Object[]) value;
                                if (arrVal.length == 0) {
                                    continue; // skip empty arrays
                                }
                                if (arrVal.length == 1) {
                                    outValue = arrVal[0].toString();
                                } else {
                                    array = true;
                                    Iterable<String> iterable = Stream.of(arrVal).map(Object::toString).collect(Collectors.toList());
                                    outValue = String.join("\n", iterable);
                                }
                            } else {
                                outValue = value.toString();
                            }

                            Tag property = new Tag("property");
                            property.addAttribute("name", apm.propName);
                            property.addAttribute("type", apm.typeName);
                            if (array) {
                                property.addContent(outValue);
                            } else {
                                property.addAttribute("value", outValue);
                            }
                            tag.addContent(property);
                        }
                    }
                }
            });

            // Automatically provide service of class Object.class if the
            // class does not already provide a service...
            if (ensureService[0]) {
                Collection<Tag> services = tag.select("service");
                if (services.isEmpty()) {
                    Tag servicesTag = new Tag("service");
                    tag.addContent(servicesTag);

                    Tag providesTag = new Tag("provide");
                    providesTag.addAttribute("interface", Object.class.getName());
                    servicesTag.addContent(providesTag);
                }
            }

        }

        return false;
    }

	/**
	 * @param value
	 * @param analyzer
	 * @return
	 */
	private String replaceProperties(String value, Properties props)
	{
		Matcher matcher = propertyPattern.matcher(value);
		StringBuffer sb = new StringBuffer();

		while(matcher.find())
		{
			String found = matcher.group(1);
			String property = props.getProperty(found);

			if(property == null)
			{
				property = "\\$\\{" + found + "\\}";
			}

			matcher.appendReplacement(sb, property);
		}
		matcher.appendTail(sb);
		return sb.toString();
	}
}