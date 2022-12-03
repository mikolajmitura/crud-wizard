package pl.jalokim.crudwizard.genericapp.mapper.generete.parser

import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createClassMetaModelFromClass
import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createValidFieldMetaModel

import java.time.LocalDateTime
import pl.jalokim.crudwizard.core.sample.SamplePersonDto
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperConfiguration
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.EachElementMapByMethodAssignExpression
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.FieldsChainToAssignExpression
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModel
import spock.lang.Unroll

class FieldChainOrEachMapByExpressionParserTest extends BaseSourceExpressionParserTestSpec {

    @Unroll
    def "return expression with two fields chain"() {
        given:
        ClassMetaModel documentMetaModel = ClassMetaModel.builder()
            .name("document")
            .fields([
                createValidFieldMetaModel("createdBy", SamplePersonDto),
            ])
            .build()

        ClassMetaModel personClassMetaModel = ClassMetaModel.builder()
            .name("person")
            .fields([
                createValidFieldMetaModel("passportData", documentMetaModel),
                createValidFieldMetaModel("personalId", documentMetaModel)
            ])
            .build()
        mapperConfiguration.getSourceMetaModel() >> personClassMetaModel

        when:
        FieldsChainToAssignExpression result = parseExpression(expression)

        then:
        result.sourceMetaModel == personClassMetaModel
        result.valueExpression == "sourceObject"
        result.fieldChains == [personClassMetaModel.getFieldByName("passportData"),
                               documentMetaModel.getFieldByName("createdBy"),
                               FieldMetaModel.builder()
                                   .fieldName("name")
                                   .fieldType(createClassMetaModelFromClass(String))
                                   .build()
        ]

        where:
        expression                          | _
        "passportData.createdBy.name"       | _
        "passportData . createdBy . name"   | _
        " passportData . createdBy . name " | _
    }

    @Unroll
    def "return expected EachElementMapByMethodAssignExpression for some field which is collection"() {
        given:
        ClassMetaModel reviewMetaModel = ClassMetaModel.builder()
            .name("review")
            .fields([
                createValidFieldMetaModel("reviewBy", String),
                createValidFieldMetaModel("reviewDateTime", LocalDateTime)
            ])
            .build()

        ClassMetaModel documentMetaModel = ClassMetaModel.builder()
            .name("document")
            .fields([
                createValidFieldMetaModel("createdBy", SamplePersonDto),
                createValidFieldMetaModel("reviews", ClassMetaModel.builder()
                    .realClass(List)
                    .genericTypes([reviewMetaModel])
                    .build()),

            ])
            .build()
        mapperConfiguration.getSourceMetaModel() >> documentMetaModel
        mapperGenerateConfiguration.getMapperConfigurationByMethodName("someInnerMethod") >> MapperConfiguration.builder().build()

        when:
        EachElementMapByMethodAssignExpression result = parseExpression(expression)
        FieldsChainToAssignExpression wrappedExpression = (FieldsChainToAssignExpression) result.wrappedExpression

        then:
        result.innerMethodName == "someInnerMethod"
        wrappedExpression.valueExpression == "sourceObject"
        wrappedExpression.fieldChains == [documentMetaModel.getFieldByName("reviews")]

        where:
        expression                               | _
        "reviews.eachMapBy(someInnerMethod)"     | _
        "reviews. eachMapBy(someInnerMethod)"    | _
        "reviews. eachMapBy (someInnerMethod)"   | _
        "reviews. eachMapBy ( someInnerMethod)"  | _
        "reviews. eachMapBy ( someInnerMethod )" | _
    }
}
