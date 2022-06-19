package pl.jalokim.crudwizard.genericapp.mapper.generete.config

import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createClassMetaModelWithParents
import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createPersonMetaModel
import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createSimpleDocumentMetaModel
import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createSomePersonClassMetaModel
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createDocumentClassMetaDto
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createValidFieldMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.exampleClassMetaModelDtoWithExtension
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.extendedPersonClassMetaModel1
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.simplePersonClassMetaModel
import static pl.jalokim.utils.test.DataFakerHelper.randomText

import java.time.LocalDate
import org.mapstruct.factory.Mappers
import pl.jalokim.crudwizard.core.sample.SamplePersonDto
import pl.jalokim.crudwizard.core.sample.SomeDto
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.FieldMetaResolverStrategyType
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelMapper
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.ByAllArgsFieldsResolver
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.BySettersFieldsResolver
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.FieldMetaResolverConfigurationDto
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.FieldMetaResolverForClassEntryDto
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperConfigurationDto
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperGenerateConfigurationDto
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.PropertiesOverriddenMappingDto
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
        def rootMapperSourceMetaModelDto = createDocumentClassMetaDto()
        def rootMapperTargetMetaModelDto = exampleClassMetaModelDtoWithExtension()
        def mapperMethodSourceMetaModelDto = simplePersonClassMetaModel()
        def mapperMethodTargetMetaModelDto = extendedPersonClassMetaModel1()
        def pathVariablesClassModel = ClassMetaModelDto.builder()
            .name("pathParamsMeta")
            .fields([
                createValidFieldMetaModelDto("usersIdVar", String),
                createValidFieldMetaModelDto("ordersIdVar", Long)
            ])
            .build()
        def requestParamsClassModel = ClassMetaModelDto.builder()
                .name("somePersonApplication-queryParams")
                .fields([
                    createValidFieldMetaModelDto("lastContact", LocalDate),
                    createValidFieldMetaModelDto("lastText", String),
                    createValidFieldMetaModelDto("numberAsText", String)])
                .build()

        def rootMapperSourceMetaModel = createSimpleDocumentMetaModel()
        def rootMapperTargetMetaModel = createClassMetaModelWithParents()
        def mapperMethodSourceMetaModel = createPersonMetaModel()
        def mapperMethodTargetMetaModel = createSomePersonClassMetaModel()

        classMetaModelMapper.toModelFromDto(rootMapperSourceMetaModelDto) >> rootMapperSourceMetaModel
        classMetaModelMapper.toModelFromDto(rootMapperTargetMetaModelDto) >> rootMapperTargetMetaModel
        classMetaModelMapper.toModelFromDto(mapperMethodSourceMetaModelDto) >> mapperMethodSourceMetaModel
        classMetaModelMapper.toModelFromDto(mapperMethodTargetMetaModelDto) >> mapperMethodTargetMetaModel

        def mapperGenerateConfigurationDto = createMapperGenerateConfigurationDto(rootMapperSourceMetaModelDto,
            rootMapperTargetMetaModelDto, mapperMethodSourceMetaModelDto, mapperMethodTargetMetaModelDto)

        when:
        def result = testCase.mapConfiguration(mapperGenerateConfigurationDto,
            pathVariablesClassModel, requestParamsClassModel)

        then:
        verifyAll(result) {
            globalEnableAutoMapping == mapperGenerateConfigurationDto.globalEnableAutoMapping
            globalIgnoreMappingProblems == mapperGenerateConfigurationDto.globalIgnoreMappingProblems
            verifyAll(fieldMetaResolverForRawTarget) {
                fieldMetaResolverStrategyType == mapperGenerateConfigurationDto.fieldMetaResolverForRawTarget.fieldMetaResolverStrategyType
                fieldMetaResolverForClass == Map.of(
                    SamplePersonDto, ByAllArgsFieldsResolver.INSTANCE,
                    SomeDto, BySettersFieldsResolver.INSTANCE
                )
            }

            verifyAll(fieldMetaResolverForRawSource) {
                fieldMetaResolverStrategyType == mapperGenerateConfigurationDto.fieldMetaResolverForRawSource.fieldMetaResolverStrategyType
                fieldMetaResolverForClass == [:]
            }

            verifyAll(rootConfiguration) {
                name == mapperGenerateConfigurationDto.rootConfiguration.name
                sourceMetaModel == rootMapperSourceMetaModel
                targetMetaModel == rootMapperTargetMetaModel
                enableAutoMapping == mapperGenerateConfigurationDto.rootConfiguration.enableAutoMapping
                ignoreMappingProblems == mapperGenerateConfigurationDto.rootConfiguration.ignoreMappingProblems
                verifyAll (propertyOverriddenMapping) {
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

            def mapperConfigurationDto = mapperGenerateConfigurationDto.subMappersAsMethods[0]

            verifyAll (mapperConfigurationByMethodName[mapperConfigurationDto.name]) {
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

    private static MapperGenerateConfigurationDto createMapperGenerateConfigurationDto(ClassMetaModelDto rootMapperSourceMetaModel,
        ClassMetaModelDto rootMapperTargetMetaModel, ClassMetaModelDto mapperMethodSourceMetaModel, ClassMetaModelDto mapperMethodTargetMetaModel) {

        MapperGenerateConfigurationDto.builder()
            .globalEnableAutoMapping(true)
            .globalIgnoreMappingProblems(false)
            .fieldMetaResolverForRawTarget(FieldMetaResolverConfigurationDto.builder()
                .fieldMetaResolverStrategyType(FieldMetaResolverStrategyType.WRITE)
                .fieldMetaResolverForClass([
                    FieldMetaResolverForClassEntryDto.builder()
                        .className(SamplePersonDto.canonicalName)
                        .resolverClassName(ByAllArgsFieldsResolver.canonicalName)
                        .build(),
                    FieldMetaResolverForClassEntryDto.builder()
                        .className(SomeDto.canonicalName)
                        .resolverClassName(BySettersFieldsResolver.canonicalName)
                        .build(),
                ])
                .build())
            .fieldMetaResolverForRawSource(FieldMetaResolverConfigurationDto.builder()
                .fieldMetaResolverStrategyType(FieldMetaResolverStrategyType.READ)
                .fieldMetaResolverForClass([])
                .build())
            .rootConfiguration(MapperConfigurationDto.builder()
                .name(randomText())
                .sourceMetaModel(rootMapperSourceMetaModel)
                .targetMetaModel(rootMapperTargetMetaModel)
                .enableAutoMapping(false)
                .ignoreMappingProblems(true)
                .propertyOverriddenMapping([
                    PropertiesOverriddenMappingDto.builder()
                        .targetAssignPath("document.name")
                        .ignoreField(true)
                        .build(),
                    PropertiesOverriddenMappingDto.builder()
                        .targetAssignPath("document.surname")
                        .ignoreField(false)
                        .build(),
                    PropertiesOverriddenMappingDto.builder()
                        .targetAssignPath("document")
                        .ignoredAllMappingProblem(true)
                        .build(),
                    PropertiesOverriddenMappingDto.builder()
                        .targetAssignPath("document.uuid")
                        .ignoreField(true)
                        .build(),
                    PropertiesOverriddenMappingDto.builder()
                        .targetAssignPath("person")
                        .ignoreField(true)
                        .build(),
                    PropertiesOverriddenMappingDto.builder()
                        .targetAssignPath("someField.next.andNext")
                        .sourceAssignExpression(randomText())
                        .build()
                ])
                .build())
            .subMappersAsMethods([
                MapperConfigurationDto.builder()
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

}
