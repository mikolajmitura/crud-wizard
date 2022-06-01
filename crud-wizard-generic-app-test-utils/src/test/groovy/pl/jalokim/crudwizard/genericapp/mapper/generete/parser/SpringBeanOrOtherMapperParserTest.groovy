package pl.jalokim.crudwizard.genericapp.mapper.generete.parser

import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createClassMetaModelFromClass
import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createValidFieldMetaModel

import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel
import pl.jalokim.crudwizard.core.metamodels.FieldMetaModel
import pl.jalokim.crudwizard.core.metamodels.MapperMetaModel
import pl.jalokim.crudwizard.core.sample.InnerDocumentDto
import pl.jalokim.crudwizard.core.sample.SamplePersonDto
import pl.jalokim.crudwizard.core.sample.SomeDocumentDto
import pl.jalokim.crudwizard.genericapp.mapper.MappersModelsCache
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ByMapperNameAssignExpression
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.BySpringBeanMethodAssignExpression
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.FieldsChainToAssignExpression
import pl.jalokim.crudwizard.genericapp.service.invoker.sample.NormalSpringService
import spock.lang.Unroll

class SpringBeanOrOtherMapperParserTest extends BaseSourceExpressionParserTestSpec {

    def setup() {
        applicationContext.getBean("normalSpringService") >> new NormalSpringService()
    }

    @Unroll
    def "return expected spring bean method expression without arguments"() {
        when:
        BySpringBeanMethodAssignExpression result = parseExpression(expression)

        then:
        result.beanType == NormalSpringService
        result.beanName == "normalSpringService"
        result.methodName == "getSomeString"
        result.methodArguments == []

        where:
        expression                                 | _
        "  @normalSpringService.getSomeString()  " | _
        "@normalSpringService.getSomeString()"     | _
        "@normalSpringService . getSomeString( )"  | _
    }

    @Unroll
    def "return expected spring bean method expression without arguments and with invocation chain"() {
        when:
        FieldsChainToAssignExpression result = parseExpression(expression)
        BySpringBeanMethodAssignExpression bySpringBeanMethodAssignExpression = (BySpringBeanMethodAssignExpression) result.getParentValueExpression()

        then:
        result.sourceMetaModel.basedOnClass == SomeDocumentDto
        result.fieldChains == [getFieldMetaModelByName(SomeDocumentDto, "documentData")]
        result.generateCodeMetadata().returnClassModel.realClass == InnerDocumentDto

        bySpringBeanMethodAssignExpression.beanType == NormalSpringService
        bySpringBeanMethodAssignExpression.beanName == "normalSpringService"
        bySpringBeanMethodAssignExpression.methodName == "getSomeDocumentDto"
        bySpringBeanMethodAssignExpression.methodArguments == []

        where:
        expression                                                     | _
        "@normalSpringService.getSomeDocumentDto().documentData"       | _
        "@normalSpringService . getSomeDocumentDto() .documentData"    | _
        " @  normalSpringService.getSomeDocumentDto(). documentData  " | _

    }

    @Unroll
    def "return expected spring bean method expression with one argument and with invocation chain"() {
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
                createValidFieldMetaModel("someDocument", documentMetaModel),
            ])
            .build()
        mapperConfiguration.getSourceMetaModel() >> personClassMetaModel

        when:
        FieldsChainToAssignExpression result = parseExpression(expression)
        BySpringBeanMethodAssignExpression bySpringBeanMethodAssignExpression = (BySpringBeanMethodAssignExpression) result.getParentValueExpression()

        then:
        result.sourceMetaModel.basedOnClass == SomeDocumentDto
        result.fieldChains == [getFieldMetaModelByName(SomeDocumentDto, "documentData")]
        result.generateCodeMetadata().returnClassModel.realClass == InnerDocumentDto

        bySpringBeanMethodAssignExpression.beanType == NormalSpringService
        bySpringBeanMethodAssignExpression.beanName == "normalSpringService"
        bySpringBeanMethodAssignExpression.methodName == "getSomeDocumentDtoById"
        bySpringBeanMethodAssignExpression.methodArguments == [
            new FieldsChainToAssignExpression(personClassMetaModel, "sourceObject", [
                personClassMetaModel.getFieldByName("someDocument"),
                documentMetaModel.getFieldByName("createdBy"),
                FieldMetaModel.builder()
                    .fieldName("id")
                    .fieldType(createClassMetaModelFromClass(Long))
                    .build()
            ])
        ]

        where:
        expression                                                                                 | _
        "@normalSpringService.getSomeDocumentDtoById(someDocument.createdBy.id).documentData"      | _
        "@normalSpringService.getSomeDocumentDtoById ( someDocument.createdBy. id) . documentData" | _

    }

    @Unroll
    def "return expected spring bean method expression with three arguments and with invocation chain"() {
        given:
        ClassMetaModel documentMetaModel = ClassMetaModel.builder()
            .name("document")
            .fields([
                createValidFieldMetaModel("createdBy", SamplePersonDto),
                createValidFieldMetaModel("someText", String),
            ])
            .build()

        ClassMetaModel personClassMetaModel = ClassMetaModel.builder()
            .name("person")
            .fields([
                createValidFieldMetaModel("someDocument", documentMetaModel),
                createValidFieldMetaModel("uuid", String),
            ])
            .build()
        mapperConfiguration.getSourceMetaModel() >> personClassMetaModel

        when:
        FieldsChainToAssignExpression result = parseExpression(expression)
        def bySpringBeanMethodAssignExpression = (BySpringBeanMethodAssignExpression) result.getParentValueExpression()

        then:
        result.sourceMetaModel.basedOnClass == SomeDocumentDto
        result.fieldChains == [getFieldMetaModelByName(SomeDocumentDto, "documentData"),
                               getFieldMetaModelByName(InnerDocumentDto, "serialNumber")]
        result.generateCodeMetadata().returnClassModel.realClass == String

        bySpringBeanMethodAssignExpression.beanType == NormalSpringService
        bySpringBeanMethodAssignExpression.beanName == "normalSpringService"
        bySpringBeanMethodAssignExpression.methodName == "getSomeDocumentDtoById"
        bySpringBeanMethodAssignExpression.methodArguments.size() == 3
        bySpringBeanMethodAssignExpression.methodArguments[0] == new FieldsChainToAssignExpression(personClassMetaModel, "sourceObject", [
            personClassMetaModel.getFieldByName("someDocument"),
            documentMetaModel.getFieldByName("createdBy"),
            getFieldMetaModelByName(SamplePersonDto, "id")
        ])

        bySpringBeanMethodAssignExpression.methodArguments[1] == new FieldsChainToAssignExpression(personClassMetaModel, "sourceObject", [
            personClassMetaModel.getFieldByName("someDocument"),
            documentMetaModel.getFieldByName("someText")
        ])

        def thirdArgumentArgument = (BySpringBeanMethodAssignExpression) bySpringBeanMethodAssignExpression.methodArguments[2]

        thirdArgumentArgument.beanType == NormalSpringService
        thirdArgumentArgument.beanName == "normalSpringService"
        thirdArgumentArgument.methodName == "someMethodName"
        thirdArgumentArgument.methodArguments == [
            new FieldsChainToAssignExpression(personClassMetaModel, "sourceObject", [
                personClassMetaModel.getFieldByName("someDocument"),
                documentMetaModel.getFieldByName("createdBy"),
                getFieldMetaModelByName(SamplePersonDto, "surname")
            ]),
            new FieldsChainToAssignExpression(personClassMetaModel, "sourceObject", [
                personClassMetaModel.getFieldByName("uuid")
            ])
        ]

        where:
        expression                                                                                                                           | _
        "@normalSpringService.getSomeDocumentDtoById(someDocument.createdBy.id," +
            " someDocument.someText, @normalSpringService.someMethodName(someDocument.createdBy.surname  , uuid)).documentData.serialNumber" | _
        "@normalSpringService. getSomeDocumentDtoById( someDocument.createdBy.id, " +
            " someDocument.someText,@normalSpringService.someMethodName(someDocument.createdBy.surname, uuid ) ).documentData.serialNumber"  | _
    }

    @Unroll
    def "return expression for mapper by name"() {
        given:
        MappersModelsCache mappersModelsCache = Mock()
        metaModelContext.getMapperMetaModels() >> mappersModelsCache

        ClassMetaModel documentMetaModel = ClassMetaModel.builder()
            .name("document")
            .fields([createValidFieldMetaModel("createdBy", SamplePersonDto)])
            .build()

        ClassMetaModel someDocumentDtoModelWrapper = ClassMetaModel.builder()
            .name("documentWrapper")
            .fields([createValidFieldMetaModel("innerDocument", SomeDocumentDto)])
            .build()

        MapperMetaModel mapperMetaModel = MapperMetaModel.builder()
            .sourceClassMetaModel(createClassMetaModelFromClass(SomeDocumentDto))
            .targetClassMetaModel(documentMetaModel)
            .build()
        mappersModelsCache.getMapperMetaModelByName("givenMapperName") >> mapperMetaModel

        mapperConfiguration.getSourceMetaModel() >> someDocumentDtoModelWrapper

        when:
        ByMapperNameAssignExpression result = parseExpression(expression)

        then:
        result.mapperName == "givenMapperName"
        result.mapperReturnClassMetaModel == documentMetaModel
        result.valueExpression == new FieldsChainToAssignExpression(someDocumentDtoModelWrapper, "sourceObject", [
            someDocumentDtoModelWrapper.getFieldByName("innerDocument")
        ])

        where:
        expression                               | _
        "@givenMapperName(innerDocument)"        | _
        " @ givenMapperName (  innerDocument ) " | _
    }

    @Unroll
    def "return expression for mapper by name with invocation chain"() {
        given:
        MappersModelsCache mappersModelsCache = Mock()
        metaModelContext.getMapperMetaModels() >> mappersModelsCache

        ClassMetaModel documentMetaModel = ClassMetaModel.builder()
            .name("document")
            .fields([createValidFieldMetaModel("createdBy", SamplePersonDto)])
            .build()

        ClassMetaModel someDocumentDtoModelWrapper = ClassMetaModel.builder()
            .name("documentWrapper")
            .fields([createValidFieldMetaModel("innerDocument", SomeDocumentDto)])
            .build()

        MapperMetaModel mapperMetaModel = MapperMetaModel.builder()
            .sourceClassMetaModel(createClassMetaModelFromClass(SomeDocumentDto))
            .targetClassMetaModel(documentMetaModel)
            .build()
        mappersModelsCache.getMapperMetaModelByName("givenMapperName") >> mapperMetaModel

        mapperConfiguration.getSourceMetaModel() >> someDocumentDtoModelWrapper

        when:
        FieldsChainToAssignExpression result = parseExpression(expression)
        ByMapperNameAssignExpression byMapperNameAssignExpression = (ByMapperNameAssignExpression) result.getParentValueExpression()

        then:
        byMapperNameAssignExpression.mapperName == "givenMapperName"
        byMapperNameAssignExpression.mapperReturnClassMetaModel == documentMetaModel
        byMapperNameAssignExpression.valueExpression == new FieldsChainToAssignExpression(someDocumentDtoModelWrapper, "sourceObject", [
            someDocumentDtoModelWrapper.getFieldByName("innerDocument")
        ])

        result.sourceMetaModel == documentMetaModel
        result.fieldChains == [documentMetaModel.getRequiredFieldByName("createdBy")]
        result.generateCodeMetadata().returnClassModel.realClass == SamplePersonDto

        where:
        expression                                            | _
        "@givenMapperName(innerDocument).createdBy"           | _
        " @ givenMapperName (  innerDocument ) . createdBy  " | _
    }
}
