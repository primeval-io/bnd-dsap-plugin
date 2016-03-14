// package io.lambdacube.component.demo.gogo.future;
//
// import org.osgi.service.component.annotations.Component;
// import org.osgi.service.component.annotations.Reference;
// import org.osgi.service.component.annotations.ReferenceCardinality;
// import org.osgi.service.component.annotations.ReferencePolicy;
//
// import io.lambdacube.component.demo.gogo.property.CommandScope;
//
// @Component
// public final class IAmGogo {
//
// @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy =
// ReferencePolicy.DYNAMIC)
// public void addCommand(@CommandScope("*") Object command) {
//
// }
//
// public void removeCommand(@CommandScope("*") Object command) {
//
// }
// }
