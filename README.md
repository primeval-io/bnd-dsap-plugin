# Bnd Declarative Services Annotation Properties Plugin
This plugin allows the definition of DS component properties using annotations

Instead of a clumsy and error-prone property definition such as this:
```
@Component(property = { "custom.secure:Boolean=true", "custom.public:Boolean=true",
        "custom.continent=AFRICA", "custom.continent=EUROPE", "service.ranking:Integer=10",
        "custom.alias=yeepee" })
public final class MyNotSoCoolComponent implements MyService {
    // methods
}
```

this plugin will enrich the XML component descriptor at compile time so you can use annotations instead:

```

@Secure(true)
@Public
@ContinentSpecific({Continent.AFRICA, Continent.EUROPE})
@Component
@ServiceRanking(10)
@Alias("yeepee")
public final class MyCoolComponent implements MyService {

}
```

It's only a matter of defining the annotations you like and annotating them with `@ComponentProperty(<propertyName>)`

```
@ComponentProperty("custom.alias")
@Target(ElementType.TYPE)
public @interface Alias {
    String value();
}
```

This generates the following XML descriptor:
```
<?xml version="1.0" encoding="UTF-8"?>
<component name="io.lambdacube.component.demo.basic.MyCoolComponent">
  <implementation class="io.lambdacube.component.demo.basic.MyCoolComponent"/>
  <service>
    <provide interface="io.lambdacube.component.demo.basic.MyService"/>
  </service>
  <property name="custom.secure" type="Boolean" value="true"/>
  <property name="custom.public" type="Boolean" value="true"/>
  <property name="custom.continent" type="String">AFRICA
EUROPE</property>
  <property name="service.ranking" type="Integer" value="10"/>
</component>

```


# Caveats

I am using Maven and maven-bundle-plugin because I am comfortable with it. Because there still isn't a release using bndlib 3.1.0, it is using 3.0.0. It's only a matter of changing the property `${bnd.version}` in the root POM to change bndlib's version, but the build will have to be adapted further until m-b-p's 3.1.0 release. 

Also, Bnd does not support extending DS component descriptors properly. This is only a dirty hack, and it's not pretty. It doesn't matter because this is build-time code and it works fine, but it would be much cleaner if Bnd had first-class support for extensions in its DS component generator mechanism.

Finally, defining the same property multiple times either through annotations or using the official `@Component(property={...})` syntax will result in the property being present several time in the XML and is likely to cause runtime problems :-).

tl;dr: it works fine, and hopefully some day it can be rewritten in a cleaner way.  


# Enabling the plugin


With Bnd:
(The plugin has to be on the classpath).

```
-plugin: io.lambdacube.bnd.component.annotation.properties.DSAPPlugin
```

With maven-bundle-plugin:
```
  <plugin>
        <groupId>org.apache.felix</groupId>
        <artifactId>maven-bundle-plugin</artifactId>
        <extensions>true</extensions>
        <version>3.0.0</version>
        <configuration>
          <instructions>
            <_plugin>
              io.lambdacube.bnd.component.annotation.properties.DSAPPlugin
            </_plugin>
          </instructions>
        </configuration>
        <dependencies>
          <dependency>
            <groupId>io.lambdacube.bnd</groupId>
            <artifactId>bnd-dsap-plugin</artifactId>
            <version>1.0-SNAPSHOT</version>
          </dependency>
        </dependencies>
      </plugin>
```

# Basic Usage

* Component property annotations must either:
  * be empty, in that case they are `Boolean` and the value is `Boolean.TRUE`.
  * have exactly one method, named `value`
    * default values are not supported (and not really useful)
    * if the return type is either `int`, `float`, `double`, `boolean`, the corresponding boxed type is used.
    * otherwise, for Strings, Enums, Classes (or Annotations themselves, discouraged), the type is String and the value is the result of a call to `Object::toString` on the annotation value.  
    * arrays are supported, and behave as you'd expect.
* You are free to use a `RUNTIME` retention if you want to also introspect the annotations at runtime, but a `SOURCE` retention will not work (as Bnd works on classes).
 
 
# Advanced Usage

It is also possible to make annotations "provide" OSGi services. 

Take the following component:
```
@Component(service = Object.class)
@CommandScope("greeting")
@MyCommandFunctions({ MyFunctions.sayHello, MyFunctions.sayGoodbye })
public final class MyComponentWithShellCommands {

    public void sayHello() {
    }

    public void sayGoodbye() {
    }
}

```

In the `@Component` annotation, we have to specify `(service = Object.class)` because of the expectations of the consuming framework (e.g Gogo shell), which is filtering on service properties but doesn't care about the service interface itself (`objectClass` property) because it uses reflection.

Because of Declarative Services' rules, if a component does not provide a service either through implementing interfaces or explicitly providing them using the `(service = ...)` definition, then no service is published to the registry. I am not sure how much sense it makes to have component properties on such a service, but that is how things are currently :-). 

If our component was providing any other service, it would not need to provide `Object.class` as well. This is just a trick to make sure it is registered as a service.

This plugin makes it possible, in these cases, to fallback to a `Object.class` without having to specify it. 

```
@ComponentProperty("osgi.command.scope")
@EnsureProvideService
@Target(ElementType.TYPE)
public @interface CommandScope {
    String value();
}
```

This feature is entirely optional, and it changes the semantic of DS slightly (for the better). Don't use `@EnsureProvideService` on your property annotations if you don't like it.


 
# Updated Gogo example

```
@Component
@CommandScope("greeting")
@MyCommandFunctions({ MyFunctions.sayHello, MyFunctions.sayGoodbye })
public final class MyComponentWithShellCommands {

    public void sayHello() {
    }

    public void sayGoodbye() {
    }
}
```

The following XML gets generated:

```
<?xml version="1.0" encoding="UTF-8"?>
<component name="io.lambdacube.component.demo.gogo.MyComponentWithShellCommands">
  <implementation class="io.lambdacube.component.demo.gogo.MyComponentWithShellCommands"/>
  <property name="osgi.command.scope" type="String" value="greeting"/>
  <property name="osgi.command.function" type="String">sayHello
sayGoodbye</property>
  <service>
    <provide interface="java.lang.Object"/>
  </service>
</component>
```


You can find the code in the `examples` sub-module.


# What's next

I am interested in using annotations on @Reference methods.

```
@Component
public final class IAmGogo {

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addCommand(@CommandScope("*") Object command) {

    }

    public void removeCommand(@CommandScope("*") Object command) {

    }
}
```

that would be equivalent to:

```
@Component
public final class IAmGogo {

    @Reference(target = "(osgi.command.scope=*)", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addCommand(Object command) {

    }

    public void removeCommand(Object command) {

    }
}


```


# License

Apache Software License, version 2.0

(c) Simon Chemouil, Lambdacube


