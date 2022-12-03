package pl.jalokim.crudwizard.genericapp.mapper.conversion

import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createClassMetaModelFromClass

import org.springframework.beans.factory.annotation.Autowired
import pl.jalokim.crudwizard.GenericAppWithReloadMetaContextSpecification
import pl.jalokim.crudwizard.core.sample.SamplePersonDto
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel
import spock.lang.Unroll

class GenericObjectsConversionServiceIT extends GenericAppWithReloadMetaContextSpecification {

    @Autowired
    private GenericObjectsConversionService genericObjectsConversionService

    @Unroll
    def "find expected convert definition and convert to expected value"() {
        when:
        def foundConverterDefinition = genericObjectsConversionService.findConverterDefinition(sourceMetaModel, targetMetaModel)
        def convertedValue = foundConverterDefinition.converter.convert(valueToConvert)

        then:
        foundConverterDefinition.beanName == expectedConverterBeanName
        convertedValue == expectedTargetValue

        where:
        sourceMetaModel               | targetMetaModel               | valueToConvert            | expectedTargetValue       | expectedConverterBeanName

        modelByClass(SamplePersonDto) | modelByClass(String)          | samplePersonDto()         | "person id:12"            | "SamplePersonDto-to-string-bean"

        modelByClass(SamplePersonDto) | modelByClass(Long)            | samplePersonDto()         | 12L                       | "SamplePersonDto-to-long-bean"

        modelByName("person")         | modelByClass(SamplePersonDto) | [personId  : 12L,
                                                                         personName: "some-name"] | samplePersonDto()         |
            "metaModelPersonToSamplePersonDto"

        modelByClass(SamplePersonDto) | modelByName("person")         | samplePersonDto()         | [personId  : 12L,
                                                                                                     personName: "some-name"] |
            "samplePersonDtoToMetaModelPerson"
        modelByName("person")         | modelByName("other-person")   | [personId  : 12L,
                                                                         personName: "some-name"] | [persId  : 12L,
                                                                                                     persName: "some-name"]   | "metaModelPersonToOtherPerson"
    }

    def "not found convert definition"() {
        when:
        def foundConverterDefinition = genericObjectsConversionService
            .findConverterDefinition(createClassMetaModelFromClass(Number.class), createClassMetaModelFromClass(Long.class))

        then:
        foundConverterDefinition == null
    }

    private static SamplePersonDto samplePersonDto() {
        SamplePersonDto.builder()
            .id(12)
            .name("some-name")
            .build()
    }

    private static ClassMetaModel modelByClass(Class<?> someClass) {
        createClassMetaModelFromClass(someClass)
    }

    private static ClassMetaModel modelByName(String classMetaModelName) {
        ClassMetaModel.builder()
            .name(classMetaModelName)
            .build()
    }
}
