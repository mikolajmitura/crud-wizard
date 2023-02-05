package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver

import pl.jalokim.crudwizard.core.sample.SomeDtoWithSetters
import pl.jalokim.crudwizard.core.sample.SomeSimpleValueDto
import pl.jalokim.crudwizard.core.sample.SuperDtoWithSuperBuilder
import pl.jalokim.crudwizard.core.utils.ReflectionUtils
import spock.lang.Unroll

class FieldMetaResolverFactoryTest extends FieldsResolverSpecification {

    @Unroll
    def "return expected #expectedFieldMetaResolver for given class: #givenClass for read"() {
        when:
        def result = FieldMetaResolverFactory.findDefaultReadFieldMetaResolverForClass(givenClass)

        then:
        result == expectedFieldMetaResolver

        where:
        expectedFieldMetaResolver           | givenClass
        ByGettersFieldsResolver.INSTANCE    | SuperDtoWithSuperBuilder
        ByGettersFieldsResolver.INSTANCE    | SomeDtoWithSetters
        ByGettersFieldsResolver.INSTANCE    | SomeSimpleValueDto
        JavaClassFieldMetaResolver.INSTANCE | List
        ByDeclaredFieldsResolver.INSTANCE   | ReflectionUtils
    }

    @Unroll
    def "return expected #expectedFieldMetaResolver for given class: #givenClass for write"() {
        when:
        def result = FieldMetaResolverFactory.findDefaultWriteFieldMetaResolverForClass(givenClass)

        then:
        result == expectedFieldMetaResolver

        where:
        expectedFieldMetaResolver           | givenClass
        ByBuilderFieldsResolver.INSTANCE    | SuperDtoWithSuperBuilder
        BySettersFieldsResolver.INSTANCE    | SomeDtoWithSetters
        ByAllArgsFieldsResolver.INSTANCE    | SomeSimpleValueDto
        JavaClassFieldMetaResolver.INSTANCE | List
        ByDeclaredFieldsResolver.INSTANCE   | ReflectionUtils
    }
}
