package pl.jalokim.crudwizard.genericapp.mapper.generete.parser

import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createClassMetaModelFromClass
import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createValidFieldMetaModel

import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperConfiguration
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperGenerateConfiguration
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.FieldsChainToAssignExpression
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.MethodInCurrentClassAssignExpression
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel
import spock.lang.Unroll

class InnerMethodSourceExpressionParserTest extends BaseSourceExpressionParserTestSpec {

    @Override
    protected MapperGenerateConfiguration createMapperGenerateConfiguration() {
        return MapperGenerateConfiguration.builder().build()
    }

    @Unroll
    def "return inner method expression as expected"() {
        given:
        def sourceExpressionParserContext = createSourceExpressionParserContext(expression, null)

        sourceExpressionParserContext.getMapperGenerateConfiguration()
            .addSubMapperConfiguration("someMethodName", MapperConfiguration.builder()
                .sourceMetaModel(createClassMetaModelFromClass(Long))
                .targetMetaModel(createClassMetaModelFromClass(String))
                .build())

        ClassMetaModel documentMetaModel = ClassMetaModel.builder()
            .name("document")
            .fields([
                createValidFieldMetaModel("personId", Long),
            ])
            .build()

        mapperConfiguration.getSourceMetaModel() >> documentMetaModel

        when:
        def result = (MethodInCurrentClassAssignExpression) initSourceExpressionParser.mainParse(
            mapperConfigurationParserContext, sourceExpressionParserContext)

        then:
        result.methodName == "someMethodName"
        result.methodArgumentsExpressions == [new FieldsChainToAssignExpression(documentMetaModel,
            "sourceObject", [documentMetaModel.getRequiredFieldByName("personId")])]
        result.methodReturnType.realClass == String

        where:
        expression                        | _
        "#someMethodName(personId)"       | _
        "#someMethodName( personId )"     | _
        " # someMethodName ( personId ) " | _
    }
}
