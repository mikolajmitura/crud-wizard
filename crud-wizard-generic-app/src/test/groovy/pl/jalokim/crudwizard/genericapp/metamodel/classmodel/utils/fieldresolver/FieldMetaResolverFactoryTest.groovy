package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver

import static FieldMetaResolverStrategyType.READ
import static FieldMetaResolverStrategyType.WRITE

import pl.jalokim.crudwizard.core.sample.SomeDtoWithSetters
import pl.jalokim.crudwizard.core.sample.SomeSimpleValueDto
import pl.jalokim.crudwizard.core.sample.SuperDtoWithSuperBuilder
import pl.jalokim.crudwizard.core.utils.ReflectionUtils
import spock.lang.Unroll

class FieldMetaResolverFactoryTest extends FieldsResolverSpecification {

    @Unroll
    def "return expected #expectedFieldMetaResolver for given class: #givenClass and #strategyType"() {
        when:
        def result = FieldMetaResolverFactory.findFieldMetaResolverForClass(givenClass, strategyType)

        then:
        result == expectedFieldMetaResolver

        where:
        expectedFieldMetaResolver        | givenClass               | strategyType
        ByGettersFieldsResolver.INSTANCE | SuperDtoWithSuperBuilder | READ
        ByGettersFieldsResolver.INSTANCE | SomeDtoWithSetters       | READ
        ByGettersFieldsResolver.INSTANCE | SomeSimpleValueDto       | READ
        ByBuilderFieldsResolver.INSTANCE | SuperDtoWithSuperBuilder | WRITE
        BySettersFieldsResolver.INSTANCE | SomeDtoWithSetters       | WRITE
        ByAllArgsFieldsResolver.INSTANCE | SomeSimpleValueDto       | WRITE
    }

    def "return expected exception"() {
        when:
        def javaClassFieldMetaResolver = FieldMetaResolverFactory.findFieldMetaResolverForClass(ReflectionUtils, WRITE)

        then:
        javaClassFieldMetaResolver == JavaClassFieldMetaResolver.INSTANCE
    }
}
