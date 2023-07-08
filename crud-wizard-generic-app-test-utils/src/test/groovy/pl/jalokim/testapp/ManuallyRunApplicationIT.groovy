package pl.jalokim.testapp

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto.buildClassMetaModelDtoWithName
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createIdFieldType
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDtoSamples.createValidFieldMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.createValidPostEndpointMetaModelDto
import static pl.jalokim.crudwizard.genericapp.metamodel.translation.TranslationDtoSamples.sampleTranslationDto

import java.time.LocalDate
import org.apache.tomcat.util.http.fileupload.FileUtils
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextService
import pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.DataStorageConnectorMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelService
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelDto
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperType
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperConfigurationDto
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.MapperGenerateConfigurationDto
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.configuration.PropertiesOverriddenMappingDto
import spock.lang.Specification

class ManuallyRunApplicationIT extends Specification {

    def setup() {
        FileUtils.deleteDirectory(new File("target/testDb"))
    }

    def "after restart application will use old generated classes"() {
        given:
        TestApplicationMain.main("--spring.config.location=classpath:/testapp/application-testapp.yaml")
        def firstSpringContext = TestApplicationMain.getSpringContext()
        def endpointMetaModelService = firstSpringContext.getBean(EndpointMetaModelService)
        def metaModelContextService = firstSpringContext.getBean(MetaModelContextService)

        def postEndpoint = createValidPostEndpointMetaModelDto().toBuilder()
            .dataStorageConnectors([
                DataStorageConnectorMetaModelDto.builder()
                    .classMetaModelInDataStorage(ClassMetaModelDto.builder()
                        .name("personEntity")
                        .fields([
                            createIdFieldType("id", Long),
                            createValidFieldMetaModelDto("code", String),
                            createValidFieldMetaModelDto("createdDate", LocalDate),
                        ])
                        .build())
                    .mapperMetaModelForPersist(
                        MapperMetaModelDto.builder()
                            .mapperName("personDtoToEntityMapper")
                            .mapperType(MapperType.GENERATED)
                            .mapperGenerateConfiguration(MapperGenerateConfigurationDto.builder()
                                .rootConfiguration(MapperConfigurationDto.builder()
                                    .name("personDtoToEntityMapper")
                                    .sourceMetaModel(buildClassMetaModelDtoWithName("personDto"))
                                    .targetMetaModel(buildClassMetaModelDtoWithName("personEntity"))
                                    .propertyOverriddenMapping([
                                        PropertiesOverriddenMappingDto.builder()
                                            .targetAssignPath("createdDate")
                                            .sourceAssignExpression("created")
                                            .build(),
                                    ])
                                    .build())
                                .build())
                            .build())
                    .build(),
            ])
            .payloadMetamodel(ClassMetaModelDto.builder()
                .name("personDto")
                .translationName(sampleTranslationDto())
                .fields([
                    createIdFieldType("id", Long),
                    createValidFieldMetaModelDto("code", String),
                    createValidFieldMetaModelDto("created", LocalDate),
                    createValidFieldMetaModelDto("otherCode", String),
                ])
                .build())
            .build()

        endpointMetaModelService.createNewEndpoint(postEndpoint)
        def metaModelContext = metaModelContextService.getMetaModelContext()
        String generatedMapperClassName = metaModelContext.mapperMetaModels.getMapperMetaModelByName("personDtoToEntityMapper")
            .mapperInstance.class.canonicalName

        TestApplicationMain.closeApplication()

        when:
        TestApplicationMain.main("--spring.config.location=classpath:/testapp/application-testapp.yaml")
        def secondSpringContext = TestApplicationMain.getSpringContext()
        def afterRestartMetaModelContextService = secondSpringContext.getBean(MetaModelContextService)
        def afterRestartMetaModelContext = afterRestartMetaModelContextService.getMetaModelContext()
        String afterRestartGeneratedMapperClassName = afterRestartMetaModelContext.mapperMetaModels
            .getMapperMetaModelByName("personDtoToEntityMapper").mapperInstance.class.canonicalName

        then:
        generatedMapperClassName == afterRestartGeneratedMapperClassName
        TestApplicationMain.closeApplication()
    }
}
