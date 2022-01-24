package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder
import static pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.FieldMetaResolverStrategyType.READ
import static pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.FieldMetaResolverStrategyType.WRITE

import pl.jalokim.crudwizard.core.exception.TechnicalException
import pl.jalokim.crudwizard.core.utils.ReflectionUtils
import pl.jalokim.crudwizard.core.sample.SomeDtoWithSetters
import pl.jalokim.crudwizard.core.sample.SomeSimpleValueDto
import pl.jalokim.crudwizard.core.sample.SuperDtoWithSuperBuilder
import pl.jalokim.crudwizard.test.utils.UnitTestSpec
import spock.lang.Unroll

class FieldMetaResolverFactoryTest extends UnitTestSpec {

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
        FieldMetaResolverFactory.findFieldMetaResolverForClass(ReflectionUtils, WRITE)

        then:
        TechnicalException ex = thrown()
        ex.message == createMessagePlaceholder(
            "cannot.find.field.resolver.strategy", ReflectionUtils.getCanonicalName()).translateMessage()

    }
}
