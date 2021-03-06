# Bnd Declarative Services Annotation Properties Plugin [![Build Status](https://travis-ci.org/primeval-io/bnd-dsap-plugin.svg?branch=master)](https://travis-ci.org/primeval-io/bnd-dsap-plugin) [![Gitter primeval-io/Lobby](https://badges.gitter.im/primeval-io/Lobby.svg)](https://gitter.im/primeval-io/Lobby)

This BND plugin allows the definition of Declarative Services component properties using Java annotations.

Tested with bnd 3.3.0.

# Maven coordinates

```xml

	<groupId>io.primeval.tooling.bnd</groupId>
	<artifactId>bnd-dsap-plugin</artifactId>
	<version>2.0.0</version>
```

# Dependencies

The `@ComponentProperty` and `@ComponentProperty` annotations reside in a separate JAR:

```xml

	<dependency>
		<groupId>io.primeval.component.annotation</groupId>
		<artifactId>component-annotation-properties</artifactId>
		<version>2.0.0</version>
	</dependency>
```

See [primeval-io/component-annotation-properties](http://github.com/primeval-io/component-annotation-properties).


# Overview


Instead of a clumsy and error-prone property definition such as this:
```java
@Component(property = { "custom.secure:Boolean=true", "custom.public:Boolean=true",
        "custom.continent=AFRICA", "custom.continent=EUROPE", "service.ranking:Integer=10",
        "custom.alias=yeepee" })
public final class MyNotSoCoolComponent implements MyService {
    // methods
}
```

this plugin will enrich the XML component descriptor at compile time so you can use annotations instead:

```java
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

```java
@ComponentProperty("custom.alias")
@Target(ElementType.TYPE)
public @interface Alias {
    String value();
}
```

This generates the following XML descriptor:
```xml
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

# `@ComponentPropertyGroup`

It is also possible to define an annotation that will potentially publish several properties using the annotation ```@ComponentPropertyGroup```.

```java
@ComponentPropertyGroup
@Target(ElementType.TYPE)
public @interface GogoCommand {

    @ComponentProperty("osgi.command.scope")
    String scope();

    @ComponentProperty("osgi.command.function")
    String[]commandFunction();
}
```

In this case, both properties are added.


# Caveats

Bnd does not support extending DS component descriptors properly. This is only a dirty hack, and it's not pretty. It doesn't matter because this is build-time code and it works fine, but it would be much cleaner if Bnd had first-class support for extensions in its DS component generator mechanism.

Finally, defining the same property multiple times either through annotations or using the official `@Component(property={...})` syntax will result in the property being present several time in the XML and is likely to cause runtime problems :-).

tl;dr: it works fine, and hopefully some day it can be rewritten in a cleaner way.  


# Enabling the plugin

You can have a look at the `examples/` build.

With Bnd:
(The plugin has to be on the classpath).

```
-plugin: io.primeval.tooling.bnd.dsap.DSAPPlugin
```

With bnd-maven-plugin:

```xml
	<plugin>
		<groupId>biz.aQute.bnd</groupId>
		<artifactId>bnd-maven-plugin</artifactId>
		<version>${bnd.version}</version>
		<configuration>
			<bnd><![CDATA[-plugin: io.primeval.tooling.bnd.dsap.DSAPPlugin]]></bnd>
		</configuration>
		<dependencies>
			<dependency>
				<groupId>io.primeval.tooling.bnd</groupId>
				<artifactId>bnd-dsap-plugin</artifactId>
				<version>2.0.0</version>
			</dependency>
		</dependencies>
		<executions>
			<execution>
				<goals>
					<goal>bnd-process</goal>
				</goals>
			</execution>
		</executions>
	</plugin>

With maven-bundle-plugin:
```xml
  <plugin>
        <groupId>org.apache.felix</groupId>
        <artifactId>maven-bundle-plugin</artifactId>
        <extensions>true</extensions>
        <version>3.0.0</version>
        <configuration>
          <instructions>
            <_plugin>
              io.primeval.tooling.bnd.dsap.DSAPPlugin
            </_plugin>
          </instructions>
        </configuration>
        <dependencies>
          <dependency>
            <groupId>io.primeval.tooling.bnd</groupId>
            <artifactId>bnd-dsap-plugin</artifactId>
            <version>2.0.0</version>
          </dependency>
        </dependencies>
      </plugin>
```


# Usage for `@ComponentProperty` on the annotation type

* `@ComponentProperty` annotations must either:
  * be empty, in that case they are `Boolean` and the value is `Boolean.TRUE`.
  * have exactly one method, named `value`
    * default values are not supported (and not really useful), except for empty arrays (in that case, the property is ignored)
    * if the return type is either `int`, `float`, `double`, `boolean`, the corresponding boxed type is used.
    * otherwise, for Strings, Enums, Classes (or Annotations themselves, discouraged), the type is String and the value is the result of a call to `Object::toString` on the annotation value.  
    * arrays are supported, and behave as you'd expect.
* You are free to use a `RUNTIME` retention if you want to also introspect the annotations at runtime, but a `SOURCE` retention will not work (as Bnd works on classes).
 
 
# Usage for `@ComponentPropertyGroup` on the annotation type

Then you put `@ComponentProperty` on your annotation methods. The annotation must not be empty, and at least one method has to be annotated with `ComponentProperty`. Rules for methods are the same as above.
 
 
# Advanced Usage

It is also possible to make annotations "provide" OSGi services. 

Take the following component:
```java
@Component(service = Object.class)
@GogoCommand(scope = "greeting", commandFunction = { "sayHello", "sayGoodbye"})
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

```java
@EnsureProvideService
@ComponentPropertyGroup
@Target(ElementType.TYPE)
public @interface GogoCommand {

    @ComponentProperty("osgi.command.scope")
    String scope();

    @ComponentProperty("osgi.command.function")
    String[]commandFunction();
}
```

This feature is entirely optional, and it changes the semantic of DS slightly (for the better). Don't use `@EnsureProvideService` on your property annotations if you don't like it.


 
# Updated Gogo example

```java
@Component
@GogoCommand(scope = "greeting", commandFunction = { "sayHello", "sayGoodbye"})
public final class MyComponentWithShellCommands {

    public void sayHello() {
    }

    public void sayGoodbye() {
    }

}
```

The following XML gets generated:

```xml
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



# Getting help

Post a new GitHub issue or join on [Gitter](https://gitter.im/primeval-io/Lobby).
 

# Author

bnd-dsap-plugin was developed by Simon Chemouil.


# Copyright

(c) 2016-2017, Simon Chemouil, Lambdacube

bnd-dsap-plugin is part of the Primeval project.
