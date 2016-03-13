package io.lambdacube.bnd.component.annotation.properties;

import java.lang.reflect.Field;
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
import io.lambdacube.component.annotation.ComponentProperty;

public final class DSAPPlugin implements AnalyzerPlugin {

    @Override
    public boolean analyzeJar(Analyzer analyzer) throws Exception {
        String sc = analyzer.getProperty(Constants.SERVICE_COMPONENT);

        if (sc == null) {
            return false;
        }

        String[] components = sc.split(",");
        Field tagField = TagResource.class.getDeclaredField("tag");
        tagField.setAccessible(true);
        for (String comp : components) {

            Resource resource = analyzer.getJar().getResource(comp);
            if (resource instanceof TagResource) {
                TagResource tagResource = (TagResource) resource;
                Tag tag = (Tag) tagField.get(tagResource);
                String className = tag.select("implementation").iterator().next().getAttribute("class");

                Clazz clazz = analyzer.getClassspace().values().stream().filter(n -> n.getClassName().getFQN().equals(className))
                        .findFirst().orElse(null);
                if (clazz == null) {
                    analyzer.warning("Couldn't find class %s, skipping", className);
                    continue;
                }

                clazz.parseClassFileWithCollector(new ClassDataCollector() {

                    @Override
                    public void annotation(Annotation annotation) throws Exception {
                        Clazz annClazz = analyzer.findClass(annotation.getName());

                        String[] propertyName = new String[1];

                        annClazz.parseClassFileWithCollector(new ClassDataCollector() {
                            public void annotation(Annotation annotation) throws Exception {
                                if (ComponentProperty.class.getName().equals(annotation.getName().getFQN())) {
                                    String v = annotation.get("value");
                                    propertyName[0] = v;
                                }
                            };
                        });

                        String prop = propertyName[0];
                        if (prop == null) {
                            return;
                        }

                        boolean[] hasMethod = new boolean[1];
                        boolean[] isArray = new boolean[1];
                        // default Type
                        String[] typeName = new String[] { "Boolean" };
                        // Read twice to be sure we only deal with the
                        // annotations we're interested in
                        annClazz.parseClassFileWithCollector(new ClassDataCollector() {

                            public void method(MethodDef last) {
                                if (!"value".equals(last.getName())) {
                                    analyzer.error(
                                            "Component property annotations can only have one field named value, found method named %s",
                                            last.getName());
                                }
                                hasMethod[0] = true;
                                String type = last.getGenericReturnType();
                                boolean arr = type.endsWith("[]");
                                isArray[0] = arr;
                                if (arr) {
                                    type = type.substring(0, type.length() - 2);
                                }
                                switch (type) {
                                case "int":
                                case "short":
                                    typeName[0] = "Integer";
                                    break;
                                case "float":
                                    typeName[0] = "Float";
                                    break;
                                case "double":
                                    typeName[0] = "Double";
                                    break;
                                case "boolean":
                                    typeName[0] = "Boolean";
                                    break;
                                default:
                                    typeName[0] = "String";
                                    break;
                                }
                            };

                        });

                        Object value = annotation.get("value");

                        if (value == null) {
                            if (hasMethod[0]) {
                                analyzer.error("Default values are unsupported, fix annotation %s", annClazz.getFQN());
                            } else {
                                value = Boolean.TRUE;
                            }
                        }

                        String outValue = null;
                        boolean array = false;
                        if (value.getClass().isArray()) {
                            Object[] arrVal = (Object[]) value;
                            if (arrVal.length == 0) {
                                analyzer.error("Empty array as value for annotation %s", annClazz.getClassName().getShortName());
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
                        property.addAttribute("name", prop);
                        property.addAttribute("type", typeName[0]);
                        if (array) {
                            property.addContent(outValue);
                        } else {
                            property.addAttribute("value", outValue);
                        }
                        tag.addContent(property);

                    }
                });
            }
        }

        return false;
    }

}