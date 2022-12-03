package pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue

import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createClassMetaModelFromClass

import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.ConstructorArgument
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MapperCodeMetadata
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperGenerateConfiguration
import pl.jalokim.crudwizard.genericapp.mapper.generete.method.MapperMethodGenerator
import pl.jalokim.crudwizard.genericapp.service.invoker.sample.NormalSpringService
import spock.lang.Specification

class BySpringBeanMethodAssignExpressionTest extends Specification {

    private ValueToAssignExpression arg1 = Mock()
    private ValueToAssignExpression arg2 = Mock()

    private MapperMethodGenerator mapperMethodGenerator = Mock()
    private MapperGenerateConfiguration mapperGenerateConfiguration = MapperGenerateConfiguration.builder().build()

    def "return expected code and metadata for given spring bean"() {
        given:
        MapperCodeMetadata mapperGeneratedCodeMetadata = new MapperCodeMetadata(mapperMethodGenerator, mapperGenerateConfiguration)
        arg1.generateCodeMetadata(mapperGeneratedCodeMetadata) >> ValueToAssignCodeMetadata.builder()
            .valueGettingCode("someCode1")
            .returnClassModel(createClassMetaModelFromClass(String))
            .build()

        arg2.generateCodeMetadata(mapperGeneratedCodeMetadata) >> ValueToAssignCodeMetadata.builder()
            .valueGettingCode("someCode2")
            .returnClassModel(createClassMetaModelFromClass(Long))
            .build()

        BySpringBeanMethodAssignExpression testCase = new BySpringBeanMethodAssignExpression(
            NormalSpringService,
            "normalSpringService",
            "someMethodName",
            [arg1, arg2]
        )

        when:
        def result = testCase.generateCodeMetadata(mapperGeneratedCodeMetadata)

        then:
        verifyAll(result) {
            returnClassModel.realClass == Long
            valueGettingCode == "normalSpringService.someMethodName(((java.lang.String) someCode1), ((java.lang.Long) someCode2))"
        }

        verifyAll(mapperGeneratedCodeMetadata) {
            staticImports.isEmpty()
            imports.isEmpty()
            constructorArguments == [ConstructorArgument.builder()
                                         .argumentName("normalSpringService")
                                         .annotations(["@org.springframework.beans.factory.annotation.Qualifier(\"normalSpringService\")"])
                                         .argumentType(NormalSpringService)
                                         .build()] as Set
        }
    }
}
