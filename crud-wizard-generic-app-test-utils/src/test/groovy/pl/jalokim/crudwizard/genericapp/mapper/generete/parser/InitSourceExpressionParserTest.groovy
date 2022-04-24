package pl.jalokim.crudwizard.genericapp.mapper.generete.parser

import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createClassMetaModelFromClass
import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createSimpleDocumentMetaModel
import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createValidFieldMetaModel

import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel
import pl.jalokim.crudwizard.core.metamodels.FieldMetaModel
import pl.jalokim.crudwizard.core.sample.SamplePersonDto
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.FieldsChainToAssignExpression
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.RawJavaCodeAssignExpression
import spock.lang.Unroll

class InitSourceExpressionParserTest extends BaseSourceExpressionParserTestSpec {

    private ClassMetaModel someClassMetaModel = createSimpleDocumentMetaModel()

    @Unroll
    def "return expression for rootSourceObject"() {
        given:
        mapperConfiguration.getSourceMetaModel() >> someClassMetaModel

        when:
        RawJavaCodeAssignExpression result = parseExpression(expression)

        then:
        result.returnClassMetaModel == someClassMetaModel
        result.rawJavaCode == "sourceObject"

        where:
        expression | _
        ""         | _
        " "        | _
        "  "       | _
    }

    @Unroll
    def "return expression with first field"() {
        given:
        ClassMetaModel classMetaModel = someClassMetaModel
        mapperConfiguration.getSourceMetaModel() >> classMetaModel

        when:
        FieldsChainToAssignExpression result = parseExpression(expression)

        then:
        result.sourceMetaModel == classMetaModel
        result.valueExpression == "sourceObject"
        result.fieldChains == [classMetaModel.getFieldByName("id")]

        where:
        expression | _
        "id"       | _
        " id "     | _
        "  id"     | _
    }

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
}
