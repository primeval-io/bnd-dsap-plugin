package io.lambdacube.component.demo.basic;

import org.osgi.service.component.annotations.Component;

@Component(property = { "custom.secure:Boolean=true", "custom.public:Boolean=true",
        "custom.continent=AFRICA", "custom.continent=EUROPE", "service.ranking:Integer=10",
        "custom.alias=yeepee" })
public final class MyNotSoCoolComponent implements MyService {
    // methods
}