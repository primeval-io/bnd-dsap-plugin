package io.lambdacube.component.demo.gogo;

import org.osgi.service.component.annotations.Component;

import io.lambdacube.component.demo.gogo.property.GogoCommand;

@Component
@GogoCommand(scope = "greeting", commandFunction = { "sayHello", "sayGoodbye"})
public final class MyComponentWithShellCommands {

    public void sayHello() {
    }

    public void sayGoodbye() {
    }

}
