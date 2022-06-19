package pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.writevalue

import static pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.writevalue.WritePropertyStrategyFactory.createWritePropertyStrategy

import pl.jalokim.crudwizard.core.sample.SomeDtoWithSetters
import pl.jalokim.crudwizard.core.sample.SomeDtoWithSuperBuilder
import pl.jalokim.crudwizard.core.sample.SomeSimpleValueDto
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel
import spock.lang.Specification
import spock.lang.Unroll

class WritePropertyStrategyFactoryTest extends Specification {

    @Unroll
    def "return expected WritePropertyStrategy for class #givenClass"() {
        when:
        def result = createWritePropertyStrategy(ClassMetaModel.builder()
            .realClass(givenClass)
            .build())

        then:
        result.getClass() == expectedStrategyClass

        where:
        givenClass               | expectedStrategyClass
        SomeSimpleValueDto.class | WriteByAllConstructorArgsStrategy.class
        SomeDtoWithSuperBuilder.class | WriteByBuilderStrategy.class
        SomeDtoWithSetters.class | WriteBySettersStrategy.class
    }
}
