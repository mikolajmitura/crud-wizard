package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils

import static pl.jalokim.crudwizard.core.translations.AppMessageSourceHolder.getMessage
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createClassMetaModelDtoFromClass
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createValidFieldMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.exampleClassMetaModelDtoWithExtension
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.simplePersonClassMetaModel
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelEntitySamples.employeePersonMetaEntity
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelEntitySamples.personMetaEntity
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getTypeMetadataFromClass

import java.time.LocalDate
import java.time.LocalDateTime
import pl.jalokim.crudwizard.core.exception.TechnicalException
import pl.jalokim.crudwizard.core.sample.SamplePersonDto
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelRepository
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.DepartmentDto
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ExtendedSamplePersonDto
import pl.jalokim.crudwizard.test.utils.UnitTestSpec
import spock.lang.Unroll

class ClassMetaModelTypeExtractorTest extends UnitTestSpec {

    static final def PERSON_CLASS_ID = 1
    static final def EMP_PERSON_CLASS_ID = 2

    ClassMetaModelRepository classMetaModelRepository = Mock()
    GenericModelTypeFactory genericModelTypeFactory = new GenericModelTypeFactory(classMetaModelRepository)
    ClassMetaModelTypeExtractor testCase = new ClassMetaModelTypeExtractor(genericModelTypeFactory)

    @Unroll
    def "return expected type by path: #givenPath"() {
        given:
        classMetaModelRepository.findExactlyOneById(PERSON_CLASS_ID) >> personMetaEntity()
        classMetaModelRepository.findExactlyOneById(EMP_PERSON_CLASS_ID) >> employeePersonMetaEntity()

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
        personDtoInPersonMetaModelEntity()                       || "surname"                              | getTypeMetadataFromClass(String.class)

        // get field from java type
        createClassMetaModelDtoFromClass(DepartmentDto)          || "headOfDepartment"                     | getTypeMetadataFromClass(SamplePersonDto.class)

        // get field from ClassMetaModelDto, entity, raw type (dto), simple field
        ClassMetaModelDto.builder()
            .name("some-name")
            .fields([
                createValidFieldMetaModelDto("somePerson", personDtoInPersonMetaModelEntity())
            ])
            .build()                                              | "somePerson.fatherData.someNumber"    || getTypeMetadataFromClass(Long.class)

        // get field from ClassMetaModelDto, entity, raw type (dto by dynamic), simple field
        ClassMetaModelDto.builder()
            .name("some-name")
            .fields([
                createValidFieldMetaModelDto("somePerson", personDtoInPersonMetaModelEntity())
            ])
            .build()                                              | "somePerson.?fatherData.someNumber"   || getTypeMetadataFromClass(Long.class)

        // get field from ClassMetaModelDto, entity, raw type (dto), simple field as dynamic field which not exist, return null
        ClassMetaModelDto.builder()
            .name("some-name")
            .fields([
                createValidFieldMetaModelDto("somePerson", personDtoInPersonMetaModelEntity())
            ])
            .build()                                              | "?somePerson.fatherData.?someDynamic" || null

        // get field from ClassMetaModelDto(extends from other) from first object
        exampleClassMetaModelDtoWithExtension()                   | "id"                                  || getTypeMetadataFromClass(Long.class)

        // get field from ClassMetaModelDto(extends from other) from second object
        exampleClassMetaModelDtoWithExtension()                   | "someNumber"                          || getTypeMetadataFromClass(Long.class)

        // get field from ClassMetaModelDto(extends from other) but name exists in current ClassMetaModelDto
        exampleClassMetaModelDtoWithExtension()                   | "birthDate"                           || getTypeMetadataFromClass(Date.class)

        // get field from entity(extends from other) from first object
        empPersonDtoInEmpPersonMetaModelEntity()                  | "name"                                || getTypeMetadataFromClass(String.class)

        // get field from entity(extends from other) from second object
        empPersonDtoInEmpPersonMetaModelEntity()                  | "headOfDepartment"                    || getTypeMetadataFromClass(SamplePersonDto.class)

        // get field from entity(extends from other) but name exists in current entity
        empPersonDtoInEmpPersonMetaModelEntity()                  | "fullName"                            || getTypeMetadataFromClass(Map.class)

        // get field from dto(extends from other)
        createClassMetaModelDtoFromClass(ExtendedSamplePersonDto) | "birthDay"                            || getTypeMetadataFromClass(LocalDate.class)

        // get field from dto(extends from other) but name exists in current class
        createClassMetaModelDtoFromClass(ExtendedSamplePersonDto) | "id"                                  || getTypeMetadataFromClass(String.class)

        // get field from dto(extends from other), entity(extends from other), raw type (dto, extends from other), simple field
        aLotOfExtensionsClassMetaModelDto()                       | "someEmpField.fatherData.lastLogin"   || getTypeMetadataFromClass(LocalDateTime.class)
    }

    @Unroll
    def "inform about invalid path"() {
        given:
        classMetaModelRepository.findExactlyOneById(PERSON_CLASS_ID) >> personMetaEntity()
        classMetaModelRepository.findExactlyOneById(EMP_PERSON_CLASS_ID) >> employeePersonMetaEntity()

        when:
        testCase.getTypeByPath(inputClassDto, givenPath)

        then:
        TechnicalException ex = thrown()
        ex.message == getMessage("ClassMetaModelTypeExtractor.invalid.path", messageArgs)

        where:
        inputClassDto                            | givenPath                                     | messageArgs
        aLotOfExtensionsClassMetaModelDto()      | "?someEmpField.ifatherData.nextField.andNext" | [currentPath    : "?someEmpField",
                                                                                                    fieldName      : "ifatherData",
                                                                                                    currentNodeType: "employee-person"]

        aLotOfExtensionsClassMetaModelDto()      | "isomeEmpField.ifatherData.nextField.andNext" | [currentPath    : "",
                                                                                                    fieldName      : "isomeEmpField",
                                                                                                    currentNodeType: "person-with-2-extends"]

        aLotOfExtensionsClassMetaModelDto()      | "someEmpField.fatherData.nextField.andNext"   | [currentPath    : "someEmpField.fatherData",
                                                                                                    fieldName      : "nextField",
                                                                                                    currentNodeType: ExtendedSamplePersonDto.canonicalName]

        ClassMetaModelDto.builder()
            .fields([
                createValidFieldMetaModelDto("department", createClassMetaModelDtoFromClass(DepartmentDto))
            ])
            .build()                             | "department.notInDepartement"                || [currentPath    : "department",
                                                                                                    fieldName      : "notInDepartement",
                                                                                                    currentNodeType: DepartmentDto.canonicalName]

        empPersonDtoInEmpPersonMetaModelEntity() | "boss.fatherData.notExists.nextNode"         || [currentPath    : "boss.fatherData",
                                                                                                    fieldName      : "notExists",
                                                                                                    currentNodeType: ExtendedSamplePersonDto.canonicalName]
    }

    def "cannot resolve path as java class"() {
        given:
        classMetaModelRepository.findExactlyOneById(PERSON_CLASS_ID) >> personMetaEntity()

        when:
        testCase.getTypeByPath(ClassMetaModelDto.builder()
            .name("some-name")
            .fields([
                createValidFieldMetaModelDto("somePerson", personDtoInPersonMetaModelEntity())
            ])
            .build(), "somePerson.passportData")

        then:
        TechnicalException ex = thrown()
        ex.message == getMessage("ClassMetaModelTypeExtractor.not.as.raw.type")
    }

    @Unroll
    def "cannot get fields from simple types"() {
        given:
        classMetaModelRepository.findExactlyOneById(PERSON_CLASS_ID) >> personMetaEntity()
        classMetaModelRepository.findExactlyOneById(EMP_PERSON_CLASS_ID) >> employeePersonMetaEntity()

        when:
        testCase.getTypeByPath(inputClassDto, givenPath)

        then:
        TechnicalException ex = thrown()
        ex.message == getMessage("ClassMetaModelTypeExtractor.not.expected.any.field", messageArgs)

        where:
        inputClassDto                      | givenPath                                     | messageArgs
        // from simple field from class meta dto
        ClassMetaModelDto.builder()
            .name("some-name")
            .fields([
                createValidFieldMetaModelDto("somePerson1", simplePersonClassMetaModel())
            ])
            .build()                       | "somePerson1.name.?dynamicInString.next"      | [currentPath    : "somePerson1.name",
                                                                                              currentNodeType: String.canonicalName]

        // from simple field from entity
        personDtoInPersonMetaModelEntity() | "passportData.validTo.?dynamicInString.next"  | [currentPath    : "passportData.validTo",
                                                                                              currentNodeType: LocalDate.canonicalName]

        // from simple field from raw dto
        personDtoInPersonMetaModelEntity() | "fatherData.someNumber.?dynamicInString.next" | [currentPath    : "fatherData.someNumber",
                                                                                              currentNodeType: Long.canonicalName]
    }

    private ClassMetaModelDto personDtoInPersonMetaModelEntity() {
        ClassMetaModelDto.builder()
            .id(PERSON_CLASS_ID)
            .build()
    }

    private ClassMetaModelDto empPersonDtoInEmpPersonMetaModelEntity() {
        ClassMetaModelDto.builder()
            .id(EMP_PERSON_CLASS_ID)
            .build()
    }

    private ClassMetaModelDto aLotOfExtensionsClassMetaModelDto() {
        def classMetaModelDto = exampleClassMetaModelDtoWithExtension().toBuilder().build()
        def firstExtension = classMetaModelDto.extendsFromModels[0]
        firstExtension.fields.add(createValidFieldMetaModelDto("someEmpField", empPersonDtoInEmpPersonMetaModelEntity()))
        classMetaModelDto
    }
}
