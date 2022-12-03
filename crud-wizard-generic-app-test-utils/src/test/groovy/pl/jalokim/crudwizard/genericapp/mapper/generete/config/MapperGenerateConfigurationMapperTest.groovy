package pl.jalokim.crudwizard.genericapp.mapper.generete.config

import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createClassMetaModelWithParents
import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createPersonMetaModel
import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createSimpleDocumentMetaModel
import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createSomePersonClassMetaModel
import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createValidFieldMetaModel
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.EnumClassMetaModel.ENUM_VALUES_PREFIX
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getFullClassName
import static pl.jalokim.utils.test.DataFakerHelper.randomText

import java.time.LocalDate
import org.mapstruct.factory.Mappers
import pl.jalokim.crudwizard.core.config.jackson.ObjectMapperConfig
import pl.jalokim.crudwizard.core.sample.SamplePersonDto
import pl.jalokim.crudwizard.core.sample.SomeDto
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.FieldMetaResolverStrategyType
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyEntity
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelEntity
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelMapper
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ExtendedSamplePersonDto
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModel
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModelEntity
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.ByAllArgsFieldsResolver
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.BySettersFieldsResolver
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContext
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.FieldMetaResolverConfigurationEntity
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.FieldMetaResolverForClassEntryEntity
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperConfigurationEntity
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperGenerateConfigurationEntity
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.PropertiesOverriddenMappingEntity
import pl.jalokim.utils.reflection.InvokableReflectionUtils
import spock.lang.Specification

class MapperGenerateConfigurationMapperTest extends Specification {

    private ClassMetaModelMapper classMetaModelMapper = Mock()
    private MapperGenerateConfigurationMapper testCase = Mappers.getMapper(MapperGenerateConfigurationMapper)

    def setup() {
        InvokableReflectionUtils.setValueForField(testCase, "classMetaModelMapper", classMetaModelMapper)
    }

    def "return expected MapperGenerateConfiguration object"() {
        given:
        def rootMapperSourceMetaModelDto = createDocumentClassMetaEntity()
        def rootMapperTargetMetaModelDto = exampleClassMetaModelDtoWithExtension()
        def mapperMethodSourceMetaModelDto = simplePersonClassMetaModel()
        def mapperMethodTargetMetaModelDto = extendedPersonClassMetaModel1()
        def pathVariablesClassModel = ClassMetaModel.builder()
            .name("pathParamsMeta")
            .fields([
                createValidFieldMetaModel("usersIdVar", String),
                createValidFieldMetaModel("ordersIdVar", Long)
            ])
            .build()
        def requestParamsClassModel = ClassMetaModel.builder()
            .name("somePersonApplication-queryParams")
            .fields([
                createValidFieldMetaModel("lastContact", LocalDate),
                createValidFieldMetaModel("lastText", String),
                createValidFieldMetaModel("numberAsText", String)])
            .build()

        def rootMapperSourceMetaModel = createSimpleDocumentMetaModel()
        def rootMapperTargetMetaModel = createClassMetaModelWithParents()
        def mapperMethodSourceMetaModel = createPersonMetaModel()
        def mapperMethodTargetMetaModel = createSomePersonClassMetaModel()

        def mapperGenerateConfigurationEntity = createMapperGenerateConfigurationEntity(rootMapperSourceMetaModelDto,
            rootMapperTargetMetaModelDto, mapperMethodSourceMetaModelDto, mapperMethodTargetMetaModelDto)

        def context = new MetaModelContext()
        context.classMetaModels.put(1, rootMapperSourceMetaModel)
        context.classMetaModels.put(2, rootMapperTargetMetaModel)
        context.classMetaModels.put(3, mapperMethodSourceMetaModel)
        context.classMetaModels.put(4, mapperMethodTargetMetaModel)

        when:
        def result = testCase.mapConfiguration(mapperGenerateConfigurationEntity,
            pathVariablesClassModel, requestParamsClassModel, context)

        then:
        verifyAll(result) {
            globalEnableAutoMapping == mapperGenerateConfigurationEntity.globalEnableAutoMapping
            globalIgnoreMappingProblems == mapperGenerateConfigurationEntity.globalIgnoreMappingProblems
            verifyAll(fieldMetaResolverForRawTarget) {
                fieldMetaResolverStrategyType == mapperGenerateConfigurationEntity.fieldMetaResolverForRawTarget.fieldMetaResolverStrategyType
                fieldMetaResolverForClass == Map.of(
                    SamplePersonDto, ByAllArgsFieldsResolver.INSTANCE,
                    SomeDto, BySettersFieldsResolver.INSTANCE
                )
            }

            verifyAll(fieldMetaResolverForRawSource) {
                fieldMetaResolverStrategyType == mapperGenerateConfigurationEntity.fieldMetaResolverForRawSource.fieldMetaResolverStrategyType
                fieldMetaResolverForClass == [:]
            }

            verifyAll(rootConfiguration) {
                name == mapperGenerateConfigurationEntity.rootConfiguration.name
                sourceMetaModel == rootMapperSourceMetaModel
                targetMetaModel == rootMapperTargetMetaModel
                enableAutoMapping == mapperGenerateConfigurationEntity.rootConfiguration.enableAutoMapping
                ignoreMappingProblems == mapperGenerateConfigurationEntity.rootConfiguration.ignoreMappingProblems
                verifyAll(propertyOverriddenMapping) {
                    ignoredFields == ["person"]
                    !ignoreMappingProblem
                    mappingsByPropertyName.keySet() == ["document", "someField", "person"] as Set

                    verifyAll(mappingsByPropertyName.get("document")) {
                        ignoredFields == ["name", "uuid"]
                        ignoreMappingProblem
                        mappingsByPropertyName.keySet() == ["name", "uuid", "surname"] as Set
                        verifyAll(mappingsByPropertyName.get("name")) {
                            ignoredFields == []
                            !ignoreMappingProblem
                            mappingsByPropertyName.keySet() == [] as Set
                        }
                    }

                    verifyAll(mappingsByPropertyName.get("someField")) {
                        ignoredFields == []
                        !ignoreMappingProblem
                        mappingsByPropertyName.keySet() == ["next"] as Set

                        verifyAll(mappingsByPropertyName.get("next")) {
                            ignoredFields == []
                            !ignoreMappingProblem
                            mappingsByPropertyName.keySet() == ["andNext"] as Set

                            verifyAll(mappingsByPropertyName.get("andNext")) {
                                ignoredFields == []
                                !ignoreMappingProblem
                                mappingsByPropertyName.keySet() == [] as Set
                                valueMappingStrategy == []
                            }
                        }
                    }
                }
            }

            mapperConfigurationByMethodName.size() == 1

            def mapperConfigurationDto = mapperGenerateConfigurationEntity.subMappersAsMethods[0]

            verifyAll(mapperConfigurationByMethodName[mapperConfigurationDto.name]) {
                name == mapperConfigurationDto.name
                sourceMetaModel == mapperMethodSourceMetaModel
                targetMetaModel == mapperMethodTargetMetaModel
                enableAutoMapping == mapperConfigurationDto.enableAutoMapping
                ignoreMappingProblems == mapperConfigurationDto.ignoreMappingProblems

                verifyAll(propertyOverriddenMapping) {
                    ignoredFields == []
                    !ignoreMappingProblem
                    valueMappingStrategy == []
                    mappingsByPropertyName.isEmpty()
                }
            }
        }
    }

    private static MapperGenerateConfigurationEntity createMapperGenerateConfigurationEntity(ClassMetaModelEntity rootMapperSourceMetaModel,
        ClassMetaModelEntity rootMapperTargetMetaModel, ClassMetaModelEntity mapperMethodSourceMetaModel, ClassMetaModelEntity mapperMethodTargetMetaModel) {

        MapperGenerateConfigurationEntity.builder()
            .globalEnableAutoMapping(true)
            .globalIgnoreMappingProblems(false)
            .fieldMetaResolverForRawTarget(FieldMetaResolverConfigurationEntity.builder()
                .fieldMetaResolverStrategyType(FieldMetaResolverStrategyType.WRITE)
                .fieldMetaResolverForClass([
                    FieldMetaResolverForClassEntryEntity.builder()
                        .className(SamplePersonDto.canonicalName)
                        .resolverClassName(ByAllArgsFieldsResolver.canonicalName)
                        .build(),
                    FieldMetaResolverForClassEntryEntity.builder()
                        .className(SomeDto.canonicalName)
                        .resolverClassName(BySettersFieldsResolver.canonicalName)
                        .build(),
                ])
                .build())
            .fieldMetaResolverForRawSource(FieldMetaResolverConfigurationEntity.builder()
                .fieldMetaResolverStrategyType(FieldMetaResolverStrategyType.READ)
                .fieldMetaResolverForClass([])
                .build())
            .rootConfiguration(MapperConfigurationEntity.builder()
                .name(randomText())
                .sourceMetaModel(rootMapperSourceMetaModel)
                .targetMetaModel(rootMapperTargetMetaModel)
                .enableAutoMapping(false)
                .ignoreMappingProblems(true)
                .propertyOverriddenMapping([
                    PropertiesOverriddenMappingEntity.builder()
                        .targetAssignPath("document.name")
                        .ignoreField(true)
                        .build(),
                    PropertiesOverriddenMappingEntity.builder()
                        .targetAssignPath("document.surname")
                        .ignoreField(false)
                        .build(),
                    PropertiesOverriddenMappingEntity.builder()
                        .targetAssignPath("document")
                        .ignoredAllMappingProblem(true)
                        .build(),
                    PropertiesOverriddenMappingEntity.builder()
                        .targetAssignPath("document.uuid")
                        .ignoreField(true)
                        .build(),
                    PropertiesOverriddenMappingEntity.builder()
                        .targetAssignPath("person")
                        .ignoreField(true)
                        .build(),
                    PropertiesOverriddenMappingEntity.builder()
                        .targetAssignPath("someField.next.andNext")
                        .sourceAssignExpression(randomText())
                        .build()
                ])
                .build())
            .subMappersAsMethods([
                MapperConfigurationEntity.builder()
                    .name(randomText())
                    .sourceMetaModel(mapperMethodSourceMetaModel)
                    .targetMetaModel(mapperMethodTargetMetaModel)
                    .enableAutoMapping(true)
                    .ignoreMappingProblems(false)
                    .propertyOverriddenMapping([])
                    .build()
            ])
            .build()
    }

    static ClassMetaModelEntity createDocumentClassMetaEntity() {
        ClassMetaModelEntity.builder()
            .name("document")
            .fields([
                createField("id", Long, [isIdFieldType()]),
                createField("type", Byte),
                createField("enumField", createEnumMetaModel("ENUM1", "ENUM2")),
                createField("value", String),
                createField("validFrom", LocalDate),
                createField("validTo", LocalDate),
            ])
            .build()
    }

    private static FieldMetaModelEntity createField(String fieldName, Class<?> fieldType,
        List<AdditionalPropertyEntity> additionalProperties = []) {
        FieldMetaModelEntity.builder()
            .fieldName(fieldName)
            .fieldType(createClassMetaModelEntityFromClass(fieldType))
            .additionalProperties(additionalProperties)
            .build()
    }

    private static FieldMetaModelEntity createField(String fieldName, ClassMetaModelEntity fieldType) {
        FieldMetaModelEntity.builder()
            .fieldName(fieldName)
            .fieldType(fieldType)
            .build()
    }

    private static ClassMetaModelEntity createClassMetaModelEntityFromClass(Class<?> metaModelClass) {
        ClassMetaModelEntity.builder()
            .className(metaModelClass.canonicalName)
            .isGenericEnumType(false)
            .build()
    }

    static ClassMetaModelEntity createEnumMetaModel(String... enumValues) {
        ClassMetaModelEntity.builder()
            .name("exampleEnum")
            .isGenericEnumType(true)
            .additionalProperties([
                AdditionalPropertyEntity.builder()
                    .name(ENUM_VALUES_PREFIX)
                    .valueRealClassName(getFullClassName(enumValues))
                    .rawJson(ObjectMapperConfig.objectToRawJson(enumValues))
                    .build()])
            .build()
    }

    static AdditionalPropertyEntity isIdFieldType() {
        AdditionalPropertyEntity.builder()
            .name(FieldMetaModel.IS_ID_FIELD)
            .build()
    }

    static ClassMetaModelEntity exampleClassMetaModelDtoWithExtension() {
        ClassMetaModelEntity.builder()
            .name("modelWithParents")
            .isGenericEnumType(false)
            .extendsFromModels([
                extendedPersonClassMetaModel1(), createClassMetaModelEntityFromClass(ExtendedSamplePersonDto)
            ])
            .fields([
                createField("birthDate", Date)
            ])
            .build()
    }

    static ClassMetaModelEntity extendedPersonClassMetaModel1() {
        def personMetaModel = simplePersonClassMetaModel().toBuilder().build()
        personMetaModel.name = "somePersonApplication"
        personMetaModel.getFields().addAll([
            createField("documents", createListWithMetaModel(createDocumentClassMetaDto()))
        ])
        personMetaModel
    }

    static ClassMetaModelEntity simplePersonClassMetaModel() {
        ClassMetaModelEntity.builder()
            .name("person")
            .isGenericEnumType(false)
            .fields([
                createField("id", Long, [isIdFieldType()]),
                createField("name", String),
                createField("surname", String),
                createField("birthDate", LocalDate)
            ])
            .build()
    }

    static ClassMetaModelEntity createListWithMetaModel(ClassMetaModelEntity classMetaModelEntity) {
        ClassMetaModelEntity.builder()
            .className(List.canonicalName)
            .genericTypes([classMetaModelEntity])
            .build()
    }

    static ClassMetaModelEntity createDocumentClassMetaDto() {
        ClassMetaModelEntity.builder()
            .name("document")
            .fields([
                createField("id", Long, [isIdFieldType()]),
                createField("type", Byte),
                createField("enumField", createEnumMetaModel("ENUM1", "ENUM2")),
                createField("value", String),
                createField("validFrom", LocalDate),
                createField("validTo", LocalDate),
            ])
            .build()
    }
}
