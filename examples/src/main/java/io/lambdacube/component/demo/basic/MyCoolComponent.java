package io.lambdacube.component.demo.basic;

import org.osgi.service.component.annotations.Component;

import io.primeval.component.annotation.properties.common.ServiceRanking;

@Secure(true)
@Public
@ContinentSpecific({Continent.AFRICA, Continent.EUROPE})
@Component
@ServiceRanking(10)
@Alias("yeepee")
public final class MyCoolComponent implements MyService {

}
