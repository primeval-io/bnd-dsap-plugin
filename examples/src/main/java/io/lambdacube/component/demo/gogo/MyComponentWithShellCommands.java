package io.lambdacube.component.demo.gogo;

import org.osgi.service.component.annotations.Component;

import io.lambdacube.component.demo.gogo.property.CommandScope;

// (why) do we need to specify the service if there a are properties anyway? 
// (why) should objectClass be treated differently ...?
@Component(service = Object.class)
@CommandScope("greeting")
@MyCommandFunctions({ MyFunctions.sayHello, MyFunctions.sayGoodbye })
public final class MyComponentWithShellCommands {

    public void sayHello() {
    }

    public void sayGoodbye() {
    }
}
