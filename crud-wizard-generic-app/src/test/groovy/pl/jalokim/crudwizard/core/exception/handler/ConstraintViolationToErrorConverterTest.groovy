package pl.jalokim.crudwizard.core.exception.handler

import static org.hibernate.validator.internal.engine.path.PathImpl.createRootPath

import javax.validation.Path
import org.hibernate.validator.internal.engine.path.PathImpl
import org.hibernate.validator.internal.metadata.aggregated.ExecutableMetaData
import pl.jalokim.crudwizard.core.rest.response.converter.ConstraintViolationToErrorConverter
import spock.lang.Specification
import spock.lang.Unroll

class ConstraintViolationToErrorConverterTest extends Specification {

    private ConstraintViolationToErrorConverter converter = new ConstraintViolationToErrorConverter()

    def "should copy error message from violation"() {
        given:
        def input = new SimpleConstraintViolation(createRootPath(), "must not be blank")

        when:
        def actual = converter.toErrorDto(input)

        then:
        actual.message == "must not be blank"
    }

    @Unroll
    def "should stringify path #propertyPath ignoring irrelevant nodes"() {
        expect:
        converter.toErrorDto(new SimpleConstraintViolation(propertyPath, "must not be blank")).property == property

        where:
        propertyPath                                                                     || property
        beanPath(property("address"))                                                    || "address"
        beanPath(property("address"), property("postalCode"))                            || "address.postalCode"
        beanPath(listProperty("address", 0), listElement(), property("postalCode"))      || "address[0].postalCode"
        beanPath(iterableProperty("address"), iterableElement(), property("postalCode")) || "address[].postalCode"
        beanPath(mapProperty("address", "home"), listElement(), property("postalCode"))  || "address[home].postalCode"
        methodPath("createUser", param("userDto"), property("address"))                  || "address"
    }

    private Path path(PathImpl root, Closure... pathAppenders) {
        pathAppenders.each {
            it(root)
        }
        return root
    }

    private Path beanPath(Closure... pathAppenders) {
        path(createRootPath(), pathAppenders)
    }

    private Path methodPath(String methodName, Closure... pathAppenders) {
        path(createMethodPath(methodName), pathAppenders)
    }

    private PathImpl createMethodPath(String methodName) {
        ExecutableMetaData metadata = Mock()
        metadata.name >> methodName
        metadata.parameterTypes >> [Object.class]
        PathImpl.createPathForExecutable(metadata)
    }

    private def property(String name) {
        {it -> it.addPropertyNode(name)}
    }

    private def iterableProperty(String name) {
        {PathImpl it -> it.addPropertyNode(name); it.makeLeafNodeIterable()}
    }

    private def listProperty(String name, int index) {
        {PathImpl it -> it.addPropertyNode(name); it.makeLeafNodeIterableAndSetIndex(index)}
    }

    private def mapProperty(String name, Object key) {
        {PathImpl it -> it.addPropertyNode(name); it.makeLeafNodeIterableAndSetMapKey(key)}
    }

    private def param(String name) {
        {it -> it.addParameterNode(name, 0)}
    }

    private def listElement() {
        {it -> it.addContainerElementNode("<list element>")}
    }

    private def iterableElement() {
        {it -> it.addContainerElementNode("<iterable element>")}
    }

}
