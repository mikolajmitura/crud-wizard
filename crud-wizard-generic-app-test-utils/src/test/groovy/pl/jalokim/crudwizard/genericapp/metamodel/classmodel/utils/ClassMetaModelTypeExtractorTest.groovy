package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils

import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createEmployeePersonMeta
import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createSimplePersonMetaModel
import static pl.jalokim.crudwizard.core.translations.AppMessageSourceHolder.getMessage
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createClassMetaModelDtoFromClass
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createValidFieldMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.exampleClassMetaModelDtoWithExtension
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.simplePersonClassMetaModel
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getTypeMetadataFromClass

import java.time.LocalDate
import java.time.LocalDateTime
import pl.jalokim.crudwizard.core.exception.TechnicalException
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel
import pl.jalokim.crudwizard.core.sample.SamplePersonDto
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.DepartmentDto
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ExtendedSamplePersonDto
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextService
import pl.jalokim.crudwizard.genericapp.metamodel.context.ModelsCache
import pl.jalokim.crudwizard.test.utils.UnitTestSpec
import spock.lang.Unroll

class ClassMetaModelTypeExtractorTest extends UnitTestSpec {

    static final PERSON_CLASS_ID = 1L
    static final EMP_PERSON_CLASS_ID = 2L

    MetaModelContextService metaModelContextService = Mock()
    GenericModelTypeFactory genericModelTypeFactory = new GenericModelTypeFactory(metaModelContextService)
    ClassMetaModelTypeExtractor testCase = new ClassMetaModelTypeExtractor(genericModelTypeFactory)

    def setup() {
        MetaModelContext metaModelContext = new MetaModelContext()
        ModelsCache<ClassMetaModel> classMetaModels = new ModelsCache<>()

        classMetaModels.put(PERSON_CLASS_ID, createSimplePersonMetaModel())
        classMetaModels.put(EMP_PERSON_CLASS_ID, createEmployeePersonMeta())

        metaModelContext.setClassMetaModels(classMetaModels)
        metaModelContextService.getMetaModelContext() >> metaModelContext
    }

    @Unroll
    def "return expected type by path: #givenPath"() {
        when:
        def result = testCase.getTypeByPath(inputClassDto, givenPath)

        then:
        result == Optional.ofNullable(expectedType)

        where:
        inputClassDto                                             | givenPath                             || expectedType

        // get field from ClassMetaModelDto
        simplePersonClassMetaModel()                              | "birthDate"                           || getTypeMetadataFromClass(LocalDate.class)

        // get field from ClassMetaModelDto by dynamic pattern
        simplePersonClassMetaModel()                              | "?birthDate"                          || getTypeMetadataFromClass(LocalDate.class)

        // get field from ClassMetaModelDto by dynamic pattern which does not exist
        simplePersonClassMetaModel()                              | "?dynamicField"                       || null

        // get field from entity
        personDtoInPersonMetaModel()                             || "surname"                              | getTypeMetadataFromClass(String.class)

        // get field from java type
        createClassMetaModelDtoFromClass(DepartmentDto)          || "headOfDepartment"                     | getTypeMetadataFromClass(SamplePersonDto.class)

        // get field from ClassMetaModelDto, entity, raw type (dto), simple field
        ClassMetaModelDto.builder()
            .name("some-name")
            .fields([
                createValidFieldMetaModelDto("somePerson", personDtoInPersonMetaModel())
            ])
            .build()                                              | "somePerson.fatherData.someNumber"    || getTypeMetadataFromClass(Long.class)

        // get field from ClassMetaModelDto, entity, raw type (dto by dynamic), simple field
        ClassMetaModelDto.builder()
            .name("some-name")
            .fields([
                createValidFieldMetaModelDto("somePerson", personDtoInPersonMetaModel())
            ])
            .build()                                              | "somePerson.?fatherData.someNumber"   || getTypeMetadataFromClass(Long.class)

        // get field from ClassMetaModelDto, entity, raw type (dto), simple field as dynamic field which not exist, return null
        ClassMetaModelDto.builder()
            .name("some-name")
            .fields([
                createValidFieldMetaModelDto("somePerson", personDtoInPersonMetaModel())
            ])
            .build()                                              | "?somePerson.fatherData.?someDynamic" || null

        // get field from ClassMetaModelDto(extends from other) from first object
        exampleClassMetaModelDtoWithExtension()                   | "id"                                  || getTypeMetadataFromClass(Long.class)

        // get field from ClassMetaModelDto(extends from other) from second object
        exampleClassMetaModelDtoWithExtension()                   | "someNumber"                          || getTypeMetadataFromClass(Long.class)

        // get field from ClassMetaModelDto(extends from other) but name exists in current ClassMetaModelDto
        exampleClassMetaModelDtoWithExtension()                   | "birthDate"                           || getTypeMetadataFromClass(Date.class)

        // get field from entity(extends from other) from first object
        empPersonDtoInEmpPersonMetaModel()                        | "name"                                || getTypeMetadataFromClass(String.class)

        // get field from entity(extends from other) from second object
        empPersonDtoInEmpPersonMetaModel()                        | "headOfDepartment"                    || getTypeMetadataFromClass(SamplePersonDto.class)

        // get field from entity(extends from other) but name exists in current entity
        empPersonDtoInEmpPersonMetaModel()                        | "fullName"                            || getTypeMetadataFromClass(Map.class)

        // get field from dto(extends from other)
        createClassMetaModelDtoFromClass(ExtendedSamplePersonDto) | "birthDay"                            || getTypeMetadataFromClass(LocalDate.class)

        // get field from dto(extends from other) but name exists in current class
        createClassMetaModelDtoFromClass(ExtendedSamplePersonDto) | "id"                                  || getTypeMetadataFromClass(String.class)

        // get field from dto(extends from other), entity(extends from other), raw type (dto, extends from other), simple field
        aLotOfExtensionsClassMetaModelDto()                       | "someEmpField.fatherData.lastLogin"   || getTypeMetadataFromClass(LocalDateTime.class)

        //get field from dto not from class meta model from context, because some dto have fields with some class metamodels, and some metamodel
        //have field with first dto.
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

    def "cannot resolve path as java class"() {
        when:
        testCase.getTypeByPath(ClassMetaModelDto.builder()
            .name("some-name")
            .fields([
                createValidFieldMetaModelDto("somePerson", personDtoInPersonMetaModel())
            ])
            .build(), "somePerson.passportData")

        then:
        TechnicalException ex = thrown()
        ex.message == getMessage("ClassMetaModelTypeExtractor.not.as.raw.type")
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

    private ClassMetaModelDto personDtoInPersonMetaModel() {
        ClassMetaModelDto.builder()
            .id(PERSON_CLASS_ID)
            .build()
    }

    private ClassMetaModelDto empPersonDtoInEmpPersonMetaModel() {
        ClassMetaModelDto.builder()
            .id(EMP_PERSON_CLASS_ID)
            .build()
    }

    private ClassMetaModelDto aLotOfExtensionsClassMetaModelDto() {
        def classMetaModelDto = exampleClassMetaModelDtoWithExtension().toBuilder().build()
        def firstExtension = classMetaModelDto.extendsFromModels[0]
        firstExtension.fields.add(createValidFieldMetaModelDto("someEmpField", empPersonDtoInEmpPersonMetaModel()))
        classMetaModelDto
    }

//    private ClassMetaModelDto
}
