package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils

import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createClassMetaModelFromClass
import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createEmployeePersonMeta
import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createPersonMetaModel
import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createSimpleDocumentMetaModel
import static pl.jalokim.crudwizard.core.translations.AppMessageSourceHolder.getMessage
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto.buildClassMetaModelDtoWithId
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto.buildClassMetaModelDtoWithName
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createClassMetaModelDtoFromClass
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createListWithMetaModel
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createValidFieldMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.exampleClassMetaModelDtoWithExtension
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.simplePersonClassMetaModel
import static pl.jalokim.utils.reflection.InvokableReflectionUtils.setValueForField

import java.time.LocalDate
import java.time.LocalDateTime
import org.mapstruct.factory.Mappers
import pl.jalokim.crudwizard.core.exception.TechnicalException
import pl.jalokim.crudwizard.core.sample.Agreement
import pl.jalokim.crudwizard.core.sample.SamplePersonDto
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyMapper
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelMapper
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelMapperImpl
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.DepartmentDto
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ExtendedSamplePersonDto
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModelMapper
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModelMapperImpl
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.SomeClassWithPrivateFields
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextService
import pl.jalokim.crudwizard.genericapp.metamodel.context.ModelsCache
import pl.jalokim.crudwizard.genericapp.metamodel.context.TemporaryMetaModelContext
import pl.jalokim.crudwizard.genericapp.metamodel.context.TemporaryModelContextHolder
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDto
import pl.jalokim.crudwizard.test.utils.UnitTestSpec
import spock.lang.Unroll

class ClassMetaModelTypeExtractorTest extends UnitTestSpec {

    static final PERSON_CLASS_ID = 1L
    static final EMP_PERSON_CLASS_ID = 2L

    MetaModelContextService metaModelContextService = Mock()
    AdditionalPropertyMapper additionalPropertyMapper = Mappers.getMapper(AdditionalPropertyMapper)
    ClassMetaModelMapper classMetaModelMapper = new ClassMetaModelMapperImpl(additionalPropertyMapper)
    FieldMetaModelMapper fieldMetaModelMapper = new FieldMetaModelMapperImpl(additionalPropertyMapper)
    ClassMetaModelTypeExtractor testCase

    def setup() {
        setValueForField(classMetaModelMapper, "fieldMetaModelMapper", fieldMetaModelMapper)
        testCase = new ClassMetaModelTypeExtractor(classMetaModelMapper)
        MetaModelContext metaModelContext = new MetaModelContext()
        ModelsCache<ClassMetaModel> classMetaModels = new ModelsCache<>()

        classMetaModels.put(PERSON_CLASS_ID, createPersonMetaModel())
        classMetaModels.put(EMP_PERSON_CLASS_ID, createEmployeePersonMeta())

        metaModelContext.setClassMetaModels(classMetaModels)
        metaModelContextService.getMetaModelContext() >> metaModelContext

        TemporaryMetaModelContext temporaryMetaModelContext = new TemporaryMetaModelContext(metaModelContext, EndpointMetaModelDto.builder().build())
        TemporaryModelContextHolder.setTemporaryContext(temporaryMetaModelContext)
    }

    def cleanup() {
        TemporaryModelContextHolder.clearTemporaryMetaModelContext()
    }

    @Unroll
    def "return expected type by path: #givenPath"() {
        when:
        def result = testCase.getTypeByPath(inputClassDto, givenPath)
        def expectedResult = Optional.ofNullable(expectedType)

        then:
        if (expectedType == null) {
            assert result == Optional.empty()
        } else {
            assert result.get().isTheSameMetaModel(expectedResult.get())
        }

        where:
        inputClassDto                                                | givenPath                             || expectedType

        // get field from ClassMetaModelDto
        simplePersonClassMetaModel()                                 | "birthDate"                           || createClassMetaModelFromClass(LocalDate.class)

        // get field from ClassMetaModelDto by dynamic pattern
        simplePersonClassMetaModel()                                 | "?birthDate"                          || createClassMetaModelFromClass(LocalDate.class)

        // get field from ClassMetaModelDto by dynamic pattern which does not exist
        simplePersonClassMetaModel()                                 | "?dynamicField"                       || null

        // get field from entity
        personDtoInPersonMetaModel()                                 | "surname"                             || createClassMetaModelFromClass(String.class)

        // get field from java type
        createClassMetaModelDtoFromClass(DepartmentDto)              | "headOfDepartment"                    ||
            createClassMetaModelFromClass(SamplePersonDto.class)

        // get field from ClassMetaModelDto, entity, raw type (dto), simple field
        ClassMetaModelDto.builder()
            .name("some-name")
            .fields([
                createValidFieldMetaModelDto("somePerson", personDtoInPersonMetaModel())
            ])
            .build()                                                 | "somePerson.fatherData.someNumber"    || createClassMetaModelFromClass(Long.class)

        // get field from ClassMetaModelDto, entity, raw type (dto by dynamic), simple field
        ClassMetaModelDto.builder()
            .name("some-name")
            .fields([
                createValidFieldMetaModelDto("somePerson", personDtoInPersonMetaModel())
            ])
            .build()                                                 | "somePerson.?fatherData.someNumber"   || createClassMetaModelFromClass(Long.class)

        // get field from ClassMetaModelDto, entity, raw type (dto), simple field as dynamic field which not exist, return null
        ClassMetaModelDto.builder()
            .name("some-name")
            .fields([
                createValidFieldMetaModelDto("somePerson", personDtoInPersonMetaModel())
            ])
            .build()                                                 | "?somePerson.fatherData.?someDynamic" || null

        // get field from ClassMetaModelDto(extends from other) from first object
        exampleClassMetaModelDtoWithExtension()                      | "id"                                  || createClassMetaModelFromClass(Long.class)

        // get field from ClassMetaModelDto(extends from other) from second object
        exampleClassMetaModelDtoWithExtension()                      | "someNumber"                          || createClassMetaModelFromClass(Long.class)

        // get field which is without getter
        createClassMetaModelDtoFromClass(SomeClassWithPrivateFields) | "id"                                  || createClassMetaModelFromClass(Long.class)

        // get field from ClassMetaModelDto(extends from other) but name exists in current ClassMetaModelDto
        exampleClassMetaModelDtoWithExtension()                      | "birthDate"                           || createClassMetaModelFromClass(LocalDate.class)

        // get field from entity(extends from other) from first object
        empPersonDtoInEmpPersonMetaModel()                           | "name"                                ||
            createClassMetaModelFromClass(DepartmentDto.DepartmentName.class)

        // get field from entity(extends from other) from second object
        empPersonDtoInEmpPersonMetaModel()                           | "headOfDepartment"                    ||
            createClassMetaModelFromClass(SamplePersonDto.class)

        // get field from entity(extends from other) but name exists in current entity
        empPersonDtoInEmpPersonMetaModel()                           | "fullName"                            || createClassMetaModelFromClass(Map.class)

        // get field from dto(extends from other)
        createClassMetaModelDtoFromClass(ExtendedSamplePersonDto)    | "birthDay"                            || createClassMetaModelFromClass(LocalDate.class)

        // get field from dto(extends from other) but name exists in current class
        createClassMetaModelDtoFromClass(ExtendedSamplePersonDto)    | "id"                                  || createClassMetaModelFromClass(Long.class)

        // get field from dto(extends from other), entity(extends from other), raw type (dto, extends from other), simple field
        aLotOfExtensionsClassMetaModelDto()                          | "someEmpField.fatherData.lastLogin"   ||
            createClassMetaModelFromClass(LocalDateTime.class)

        // get field which is not raw java type
        ClassMetaModelDto.builder()
            .name("some-name")
            .fields([
                createValidFieldMetaModelDto("somePerson", personDtoInPersonMetaModel())
            ])
            .build()                                                 | "somePerson.passportData"             || createSimpleDocumentMetaModel()

        // crete class metamodel and usage of BY_NAME type definition.
        buildPersonMetaModel()                                       | "parent.mainDocument.serialNumber"    || createClassMetaModelFromClass(String.class)
    }

    private static ClassMetaModelDto buildPersonMetaModel() {
        ClassMetaModelDto.builder()
            .name("person")
            .fields([
                createValidFieldMetaModelDto("id", Long),
                createValidFieldMetaModelDto("code", String),
                createValidFieldMetaModelDto("parent", buildClassMetaModelDtoWithName("person")),
                createValidFieldMetaModelDto("mainDocument", buildClassMetaModelDtoWithName("document")),
                createValidFieldMetaModelDto("expiredDocuments",
                    createListWithMetaModel(buildClassMetaModelDtoWithName("document"))),
                createValidFieldMetaModelDto("documents", createListWithMetaModel(
                    ClassMetaModelDto.builder()
                        .name("document")
                        .fields([
                            createValidFieldMetaModelDto("uuid", String),
                            createValidFieldMetaModelDto("serialNumber", String),
                        ])
                        .build()
                )),
                createValidFieldMetaModelDto("oldAgreements", createListWithMetaModel(
                    ClassMetaModelDto.builder()
                        .className(Agreement.canonicalName)
                        .build()
                )),
                createValidFieldMetaModelDto("currentAgreement",
                    ClassMetaModelDto.builder()
                        .className(Agreement.canonicalName)
                        .build()
                )
            ])
            .build()
    }

    @Unroll
    def "inform about invalid path"() {
        when:
        testCase.getTypeByPath(inputClassDto, givenPath)

        then:
        TechnicalException ex = thrown()
        ex.message == getMessage("ClassMetaModelTypeExtractor.invalid.path", messageArgs)

        where:
        inputClassDto                       | givenPath                                     | messageArgs
        aLotOfExtensionsClassMetaModelDto() | "?someEmpField.ifatherData.nextField.andNext" | [currentPath    : "?someEmpField",
                                                                                               fieldName      : "ifatherData",
                                                                                               currentNodeType: "employee-person"]

        aLotOfExtensionsClassMetaModelDto() | "isomeEmpField.ifatherData.nextField.andNext" | [currentPath    : "",
                                                                                               fieldName      : "isomeEmpField",
                                                                                               currentNodeType: "person-with-2-extends"]

        aLotOfExtensionsClassMetaModelDto() | "someEmpField.fatherData.nextField.andNext"   | [currentPath    : "someEmpField.fatherData",
                                                                                               fieldName      : "nextField",
                                                                                               currentNodeType: ExtendedSamplePersonDto.canonicalName]

        ClassMetaModelDto.builder()
            .name("some-model")
            .fields([
                createValidFieldMetaModelDto("department", createClassMetaModelDtoFromClass(DepartmentDto))
            ])
            .build()                        | "department.notInDepartement"                || [currentPath    : "department",
                                                                                               fieldName      : "notInDepartement",
                                                                                               currentNodeType: DepartmentDto.canonicalName]

        empPersonDtoInEmpPersonMetaModel()  | "boss.fatherData.notExists.nextNode"         || [currentPath    : "boss.fatherData",
                                                                                               fieldName      : "notExists",
                                                                                               currentNodeType: ExtendedSamplePersonDto.canonicalName]
    }

    @Unroll
    def "cannot get fields from simple types"() {
        when:
        testCase.getTypeByPath(inputClassDto, givenPath)

        then:
        TechnicalException ex = thrown()
        ex.message == getMessage("ClassMetaModelTypeExtractor.not.expected.any.field", messageArgs)

        where:
        inputClassDto                | givenPath                                     | messageArgs
        // from simple field from class meta dto
        ClassMetaModelDto.builder()
            .name("some-name")
            .fields([
                createValidFieldMetaModelDto("somePerson1", simplePersonClassMetaModel())
            ])
            .build()                 | "somePerson1.name.?dynamicInString.next"      | [currentPath    : "somePerson1.name",
                                                                                        currentNodeType: String.canonicalName]

        // from simple field from entity
        personDtoInPersonMetaModel() | "passportData.validTo.?dynamicInString.next"  | [currentPath    : "passportData.validTo",
                                                                                        currentNodeType: LocalDate.canonicalName]

        // from simple field from raw dto
        personDtoInPersonMetaModel() | "fatherData.someNumber.?dynamicInString.next" | [currentPath    : "fatherData.someNumber",
                                                                                        currentNodeType: Long.canonicalName]
    }

    private static ClassMetaModelDto personDtoInPersonMetaModel() {
        buildClassMetaModelDtoWithId(PERSON_CLASS_ID)
    }

    private static ClassMetaModelDto empPersonDtoInEmpPersonMetaModel() {
        buildClassMetaModelDtoWithId(EMP_PERSON_CLASS_ID)
    }

    private static ClassMetaModelDto aLotOfExtensionsClassMetaModelDto() {
        def classMetaModelDto = exampleClassMetaModelDtoWithExtension().toBuilder().build()
        def firstExtension = classMetaModelDto.extendsFromModels[0]
        firstExtension.fields.add(createValidFieldMetaModelDto("someEmpField", empPersonDtoInEmpPersonMetaModel()))
        classMetaModelDto
    }
}
