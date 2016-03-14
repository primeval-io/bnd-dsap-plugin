package io.lambdacube.component.demo.gogo;

import org.osgi.service.component.annotations.Component;

import io.lambdacube.component.demo.gogo.property.CommandScope;

@Component
@CommandScope("greeting")
@MyCommandFunctions({ MyFunctions.sayHello, MyFunctions.sayGoodbye })
public final class MyComponentWithShellCommands {

    public void sayHello() {
    }

    public void sayGoodbye() {
    }

}
