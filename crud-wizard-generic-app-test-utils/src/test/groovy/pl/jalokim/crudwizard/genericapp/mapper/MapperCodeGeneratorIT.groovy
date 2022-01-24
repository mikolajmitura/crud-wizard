package pl.jalokim.crudwizard.genericapp.mapper

import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createClassMetaModelFromClass
import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createValidFieldMetaModel
import static pl.jalokim.crudwizard.genericapp.mapper.generete.ClassMetaModelDescribeHelper.getClassModelInfoForGeneratedCode

import java.time.LocalDate
import java.time.LocalDateTime
import org.springframework.beans.factory.annotation.Autowired
import pl.jalokim.crudwizard.GenericAppWithReloadMetaContextSpecification
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel
import pl.jalokim.crudwizard.core.sample.SamplePersonDto
import pl.jalokim.crudwizard.core.sample.SomeDtoWithBuilder
import pl.jalokim.crudwizard.core.sample.SomeDtoWithSetters
import pl.jalokim.crudwizard.core.sample.SomeDtoWithSimpleSuperBuilder
import pl.jalokim.crudwizard.core.sample.SomeSimpleValueDto
import pl.jalokim.crudwizard.genericapp.mapper.generete.MapperCodeGenerator
import pl.jalokim.crudwizard.genericapp.mapper.generete.MapperConfiguration
import pl.jalokim.utils.file.FileUtils
import pl.jalokim.utils.template.TemplateAsText
import spock.lang.Unroll

class MapperCodeGeneratorIT extends GenericAppWithReloadMetaContextSpecification {

    public static final MapperConfiguration EMPTY_CONFIG = MapperConfiguration.builder().build()

    @Autowired
    MapperCodeGenerator mapperGenerator

    @Unroll
    def "return expected code for #expectedFileName"() {
        when:
        def result = mapperGenerator.generateMapperCodeMetadata(sourceMetaModel, targetMetaModel, mapperConfiguration)

        then:
        def folderPath = "target/generated-test-sources/mappers/pl/jalokim/crudwizard/generated/mapper"
        FileUtils.createDirectories(folderPath)
        def mapperClassName = String.format("%s_To_%s_Mapper",
            getClassModelInfoForGeneratedCode(sourceMetaModel),
            getClassModelInfoForGeneratedCode(targetMetaModel)
        )
        FileUtils.writeToFile(String.format("%s/%s.java", folderPath, mapperClassName), result)
        result == TemplateAsText.fromClassPath("expectedCode/" + expectedFileName).currentTemplateText

        where:
        sourceMetaModel                   | targetMetaModel                         | mapperConfiguration | expectedFileName
        modelFromClass(Long)              | modelFromClass(Long)                    | EMPTY_CONFIG        | "simple_Long_to_Long"

        modelFromClass(Long)              | modelFromClass(String)                  | EMPTY_CONFIG        | "simple_Long_to_String"

        modelFromClass(SamplePersonDto)   | getPersonMetaModel()                    | EMPTY_CONFIG        | "class_SamplePersonDto_to_model_person"

        // mapping from map to Dto via builder
        getPersonMetaModel()              | modelFromClass(SamplePersonDto)         | EMPTY_CONFIG        | "model_person_to_class_SamplePersonDto"

        // mapping from map to Dto via builder, should get fields only from SomeDtoWithBuilder,
        // not from upper class due to @Builder only on SomeDtoWithBuilder classs
        getSomeDtoWithBuilderModel()      | modelFromClass(SomeDtoWithBuilder)      | EMPTY_CONFIG        |
            "model_someDtoWithBuilder_to_class_SomeDtoWithBuilder"

        // mapping from map to Dto via builder, should get fields from whole @SuperBuilder hierarchy
        getSomeDtoWithSuperBuilderModel() | modelFromClass(SomeDtoWithSimpleSuperBuilder) | EMPTY_CONFIG |
            "model_someDtoWithSuperBuilderModel_to_class_SomeDtoWithSuperBuilder"

        // mapping from map to simple Dto via all args
        getSomeSimpleValueDtoModel() | modelFromClass(SomeSimpleValueDto) | EMPTY_CONFIG | "model_SomeSimpleValueDtoModel_to_class_SomeSimpleValueDto"

        // mapping from map to simple Dto via setters
        getSomeDtoWithSettersModel() | modelFromClass(SomeDtoWithSetters) | EMPTY_CONFIG | "model_SomeDtoWithSettersModel_to_class_SomeDtoWithSetters"

        // TODO #1 mapping from map to Dto with nested methods and should use method when inner conversion is from document to DocumentDto in few fields
        //  DocumentDto idDocument -> document idDocument
        //  DocumentDto passportDocument -> document passportDocument
        // TODO #1 test for override field by get properties and by spring bean
        // TODO #1 test for not found field in source field.
        // TODO #1 test for ignoredFields during map to target
        // TODO #1 test for ignoreMappingProblem during map to target
        // TODO #1 test for mapping from enum to metamodel of enum (should looks for matched enums and inform when cannot find)
        // TODO #1 test for mapping from enum to enum (should looks for matched enums and inform when cannot find)
        // TODO #1 test for mapping from metamodel of enum to enum (should looks for matched enums and inform when cannot find)
        // TODO #1 test for mapping from string to metamodel of enum
        // TODO #1 test for mapping from metamodel of enum to string
        // TODO #1 test for mapping from some metamodel to some Dto with map inside (SomeDtoWithSuperBuilder has in hierarchy)
        // TODO #1 test for mapping where is list of objects and that nested object mapper should be provided as well for other objects,
        //  maybe should be be mappers in one mapper? but how that will be resolved in
    }

    private static ClassMetaModel getSomeDtoWithBuilderModel() {
        ClassMetaModel.builder()
            .name("someDtoWithBuilder")
            .fields([
                createValidFieldMetaModel("test1", String),
                createValidFieldMetaModel("name", String),
                createValidFieldMetaModel("testLong1", Long),
                createValidFieldMetaModel("someId", Long),
                createValidFieldMetaModel("localDateTime1", LocalDateTime),
            ])
            .build()
    }

    private static ClassMetaModel getPersonMetaModel() {
        ClassMetaModel.builder()
            .name("person")
            .fields([
                createValidFieldMetaModel("id", Long),
                createValidFieldMetaModel("name", String),
                createValidFieldMetaModel("surname", String),
                createValidFieldMetaModel("birthDay", LocalDate),
                createValidFieldMetaModel("lastLogin", LocalDateTime),
            ])
            .build()
    }

    private static modelFromClass(Class<?> someClass) {
        createClassMetaModelFromClass(someClass)
    }

    private static ClassMetaModel getSomeDtoWithSuperBuilderModel() {
        ClassMetaModel.builder()
            .name("someDtoWithSuperBuilderModel")
            .fields([
                createValidFieldMetaModel("someString1", String)
            ])
            .extendsFromModels([
                ClassMetaModel.builder()
                    .name("superDtoWithSuperBuilderModel")
                    .fields([
                        createValidFieldMetaModel("someLong1", Long),
                        createValidFieldMetaModel("superStringField", String),
                        createValidFieldMetaModel("localDateTime1", LocalDateTime),
                    ])
                    .build()
            ])
            .build()
    }

    private static ClassMetaModel getSomeSimpleValueDtoModel() {
        ClassMetaModel.builder()
            .name("someSimpleValueDtoModel")
            .fields([
                createValidFieldMetaModel("someLong3", Long),
                createValidFieldMetaModel("someString3", String),
                createValidFieldMetaModel("someDataTime3", LocalDateTime),
            ])
        .build()
    }

    private static ClassMetaModel getSomeDtoWithSettersModel() {
        ClassMetaModel.builder()
            .name("someDtoWithSettersMode")
            .fields([
                createValidFieldMetaModel("someString2", String),
                createValidFieldMetaModel("someLong2", Long),
                createValidFieldMetaModel("id", Integer),
                createValidFieldMetaModel("name", String),
                createValidFieldMetaModel("surname", String),
                createValidFieldMetaModel("birthDay", LocalDateTime),
                createValidFieldMetaModel("lastLogin", LocalDateTime),
            ])
        .build()
    }
}
