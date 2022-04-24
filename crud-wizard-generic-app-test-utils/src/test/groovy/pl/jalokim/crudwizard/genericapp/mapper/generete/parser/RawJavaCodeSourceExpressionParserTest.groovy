package pl.jalokim.crudwizard.genericapp.mapper.generete.parser

import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createClassMetaModelFromClass

import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.BySpringBeanMethodAssignExpression
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.RawJavaCodeAssignExpression
import pl.jalokim.crudwizard.genericapp.service.invoker.sample.NormalSpringService
import spock.lang.Unroll

class RawJavaCodeSourceExpressionParserTest extends BaseSourceExpressionParserTestSpec {

    @Unroll
    def "return expected simple raw expression"() {
        given:
        def targetClassMetaModel = createClassMetaModelFromClass(Long)

        when:
        RawJavaCodeAssignExpression result = parseExpression(expression, targetClassMetaModel)

        then:
        result.returnClassMetaModel == targetClassMetaModel
        result.rawJavaCode == "longVariable + otherVariable"

        where:
        expression                           | _
        "j(longVariable + otherVariable)"    | _
        "j (longVariable + otherVariable) "  | _
        " j (longVariable + otherVariable) " | _
    }

    @Unroll
    def "return expected raw expression with few brackets"() {
        given:
        def targetClassMetaModel = createClassMetaModelFromClass(Long)

        when:
        RawJavaCodeAssignExpression result = parseExpression(expression, targetClassMetaModel)

        then:
        result.returnClassMetaModel == targetClassMetaModel
        result.rawJavaCode == "(long) someMap.get(\"key\") + ((long) otherVariable)"

        where:
        expression                                                   | _
        "j((long) someMap.get(\"key\") + ((long) otherVariable))"    | _
        "j ((long) someMap.get(\"key\") + ((long) otherVariable)) "  | _
        " j ((long) someMap.get(\"key\") + ((long) otherVariable)) " | _
    }

    @Unroll
    def "return expected simple raw expression with used cast"() {
        when:
        RawJavaCodeAssignExpression result = parseExpression(expression)

        then:
        result.returnClassMetaModel.realClass == String
        result.rawJavaCode == "longVariable + otherVariable"

        where:
        expression                                                  | _
        "((c_java.lang.String)j(longVariable + otherVariable))"     | _
        "((c_java.lang.String) j (longVariable + otherVariable)  )" | _
        "((c_java.lang.String) j (longVariable + otherVariable)  )" | _
    }

    @Unroll
    def "return inner mapper method with argument as simple raw java code"() {
        given:
        applicationContext.getBean("normalSpringService") >> new NormalSpringService()
        def targetClassMetaModel = createClassMetaModelFromClass(Long)

        when:
        BySpringBeanMethodAssignExpression result = parseExpression(expression, targetClassMetaModel)

        then:
        result.beanName == "normalSpringService"
        result.beanType == NormalSpringService
        result.methodName == "getSomeDocumentDtoById"
        result.methodArguments.size() == 1
        RawJavaCodeAssignExpression rawJavaCodeAssignExpression = (RawJavaCodeAssignExpression) result.methodArguments[0]
        rawJavaCodeAssignExpression.returnClassMetaModel == targetClassMetaModel
        rawJavaCodeAssignExpression.rawJavaCode == "someVariable + otherVal"

        where:
        expression                                                                  | _
        "@normalSpringService.getSomeDocumentDtoById ( j(someVariable + otherVal))" | _
        "@normalSpringService.getSomeDocumentDtoById(j(someVariable + otherVal))"   | _
    }

    @Unroll
    def "return inner mapper method with argument as simple raw java code with used cast"() {
        given:
        applicationContext.getBean("normalSpringService") >> new NormalSpringService()

        when:
        BySpringBeanMethodAssignExpression result = parseExpression(expression)

        then:
        result.beanName == "normalSpringService"
        result.beanType == NormalSpringService
        result.methodName == "getSomeDocumentDtoById"
        result.methodArguments.size() == 1
        RawJavaCodeAssignExpression rawJavaCodeAssignExpression = (RawJavaCodeAssignExpression) result.methodArguments[0]
        rawJavaCodeAssignExpression.returnClassMetaModel.realClass == Long
        rawJavaCodeAssignExpression.rawJavaCode == "someMap.get(\"someKey\")"

        where:
        expression                                                                                        | _
        "@normalSpringService.getSomeDocumentDtoById(((c_java.lang.Long)j(someMap.get(\"someKey\"))))"    | _
        "@normalSpringService.getSomeDocumentDtoById ( ((c_java.lang.Long) j(someMap.get(\"someKey\"))))" | _
    }
}
