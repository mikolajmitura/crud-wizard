package pl.jalokim.crudwizard.genericapp.mapper.generete.parser

import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelConstants.STRING_MODEL
import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createValidFieldMetaModel

import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.FieldsChainToAssignExpression
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.RawJavaCodeAssignExpression
import spock.lang.Unroll

class OtherVariableSourceExpressionParserTest extends BaseSourceExpressionParserTestSpec {

    @Unroll
    def "return header param as expected"() {
        when:
        RawJavaCodeAssignExpression result = parseExpression(expression)

        then:
        result.returnClassMetaModel == STRING_MODEL
        result.rawJavaCode == 'headers.get("cookies")'
        result.generateCodeMetadata().returnClassModel.realClass == String

        where:
        expression                 | _
        "\$headers['cookies']"     | _
        "\$headers [ 'cookies' ] " | _
        "\$headers.cookies"        | _
        "\$headers . cookies "     | _
        " \$headers . cookies "    | _
    }

    @Unroll
    def "return pathVariables param as expected"() {
        given:
        def paramsMetaModel = ClassMetaModel.builder()
            .name("params")
            .fields([
                createValidFieldMetaModel("query1", Long)
            ])
            .build()

        mapperGenerateConfiguration.getPathVariablesClassModel() >> paramsMetaModel

        when:
        FieldsChainToAssignExpression result = parseExpression(expression)

        then:
        result.valueExpression == 'pathVariables'
        result.sourceMetaModel == paramsMetaModel
        result.fieldChains == [paramsMetaModel.getRequiredFieldByName("query1")]
        result.generateCodeMetadata().returnClassModel.realClass == Long

        where:
        expression                      | _
        "\$pathVariables['query1']"     | _
        "\$pathVariables [ 'query1' ] " | _
        "\$pathVariables.query1"        | _
        "\$pathVariables . query1 "     | _
        " \$pathVariables . query1 "    | _
    }

    @Unroll
    def "return requestParams param as expected"() {
        given:
        def requestParamsMetaModel = ClassMetaModel.builder()
            .name("requestParams")
            .fields([
                createValidFieldMetaModel("query2", Integer)
            ])
            .build()

        mapperGenerateConfiguration.getRequestParamsClassModel() >> requestParamsMetaModel

        when:
        FieldsChainToAssignExpression result = parseExpression(expression)

        then:
        result.valueExpression == 'requestParams'
        result.sourceMetaModel == requestParamsMetaModel
        result.fieldChains == [requestParamsMetaModel.getRequiredFieldByName("query2")]
        result.generateCodeMetadata().returnClassModel.realClass == Integer

        where:
        expression                      | _
        "\$requestParams['query2']"     | _
        "\$requestParams [ 'query2' ] " | _
        "\$requestParams.query2"        | _
        "\$requestParams . query2 "     | _
        " \$requestParams . query2 "    | _
    }

    @Unroll
    def "return source param as expected"() {
        def personMetaModel = ClassMetaModel.builder()
            .name("person")
            .fields([
                createValidFieldMetaModel("surname", String),
                createValidFieldMetaModel("name", String)
            ])
            .build()

        def documentMetaModel = ClassMetaModel.builder()
            .name("document")
            .fields([
                createValidFieldMetaModel("id", Integer),
                createValidFieldMetaModel("owner", personMetaModel)
            ])
            .build()

        given:
        mapperConfiguration.getSourceMetaModel() >> documentMetaModel

        when:
        FieldsChainToAssignExpression result = parseExpression(expression)

        then:
        result.valueExpression == 'sourceObject'
        result.sourceMetaModel == documentMetaModel
        result.fieldChains == [
            documentMetaModel.getRequiredFieldByName("owner"),
            personMetaModel.getRequiredFieldByName("surname")
        ]
        result.generateCodeMetadata().returnClassModel.realClass == String

        where:
        expression                                | _
        "\$sourceObject['owner'].surname"     | _
        "\$sourceObject [ 'owner' ]. surname" | _
        "\$sourceObject.owner.surname"        | _
        "\$sourceObject . owner. surname"     | _
        " \$sourceObject . owner . surname "  | _
    }

    @Unroll
    def "return root source param as expected"() {
        def personMetaModel = ClassMetaModel.builder()
            .name("person")
            .fields([
                createValidFieldMetaModel("surname", String),
                createValidFieldMetaModel("name", String)
            ])
            .build()

        def documentMetaModel = ClassMetaModel.builder()
            .name("document")
            .fields([
                createValidFieldMetaModel("id", Integer),
                createValidFieldMetaModel("owner", personMetaModel)
            ])
            .build()

        given:
        mapperGenerateConfiguration.getRootConfiguration() >> mapperConfiguration
        mapperConfiguration.getSourceMetaModel() >> documentMetaModel

        when:
        FieldsChainToAssignExpression result = parseExpression(expression)

        then:
        result.valueExpression == 'rootSourceObject'
        result.sourceMetaModel == documentMetaModel
        result.fieldChains == [
            documentMetaModel.getRequiredFieldByName("owner"),
            personMetaModel.getRequiredFieldByName("surname")
        ]
        result.generateCodeMetadata().returnClassModel.realClass == String

        where:
        expression                                | _
        "\$rootSourceObject['owner'].surname"     | _
        "\$rootSourceObject [ 'owner' ]. surname" | _
        "\$rootSourceObject.owner.surname"        | _
        "\$rootSourceObject . owner. surname"     | _
        " \$rootSourceObject . owner . surname "  | _
    }

    @Unroll
    def "return expected expression after cast for mappingContext variable param"() {
        when:
        RawJavaCodeAssignExpression result = parseExpression(expression)

        then:
        result.returnClassMetaModel == STRING_MODEL
        result.rawJavaCode == 'mappingContext.get("personId")'
        result.generateCodeMetadata().returnClassModel.realClass == String

        where:
        expression                                              | _
        "((c_java.lang.String)\$mappingContext.personId)"       | _
        "((c_java.lang.String) \$mappingContext.personId)"      | _
        "((c_java.lang.String) \$ mappingContext . personId ) " | _
    }

    @Unroll
    def "return expected expression after cast for mappingContext variable param with chain fields"() {
        given:
        def personMetaModel = ClassMetaModel.builder()
            .name("person")
            .fields([
                createValidFieldMetaModel("surname", String),
                createValidFieldMetaModel("name", String)
            ])
            .build()

        metaModelContextService.getClassMetaModelByName("person") >> personMetaModel

        when:
        FieldsChainToAssignExpression result = parseExpression(expression)
        def mappingContextExpressionWithPerson = (RawJavaCodeAssignExpression) result.parentValueExpression

        then:
        mappingContextExpressionWithPerson.returnClassMetaModel == personMetaModel
        mappingContextExpressionWithPerson.rawJavaCode == 'mappingContext.get("returnPerson")'
        mappingContextExpressionWithPerson.generateCodeMetadata().returnClassModel.realClass == null

        result.sourceMetaModel == personMetaModel
        result.fieldChains == [personMetaModel.getRequiredFieldByName("name")]
        result.generateCodeMetadata().returnClassModel.realClass == String

        where:
        expression                                              | _
        "((m_person)\$mappingContext.returnPerson).name"        | _
        "((m_person) \$mappingContext.returnPerson) . name"     | _
        "((m_person) \$ mappingContext . returnPerson ) .name " | _
    }
}
