package pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue

import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createValidFieldMetaModel
import static pl.jalokim.crudwizard.genericapp.mapper.generete.GeneratedLineUtils.wrapWithNextLineWith3Tabs

import java.time.LocalDate
import pl.jalokim.crudwizard.core.sample.SamplePersonDto
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MapperCodeMetadata
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperGenerateConfiguration
import pl.jalokim.crudwizard.genericapp.mapper.generete.method.MapperMethodGenerator
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel
import pl.jalokim.crudwizard.genericapp.rest.samples.dto.SampleDtoWithoutGetter
import pl.jalokim.utils.reflection.InvokableReflectionUtils
import spock.lang.Specification

class FieldsChainToAssignExpressionTest extends Specification {

    MapperMethodGenerator mapperMethodGenerator = Mock()

    def "return expected method mapping chain"() {
        given:
        MapperCodeMetadata mapperGeneratedCodeMetadata = new MapperCodeMetadata(mapperMethodGenerator,
            MapperGenerateConfiguration.builder().build())

        def birthDatFieldModel = createValidFieldMetaModel("birthDay", LocalDate)
        def samplePersonFieldModel = createValidFieldMetaModel("samplePersonDto", SamplePersonDto)
        def personWrapperField = createValidFieldMetaModel("personWrapper", SampleDtoWithoutGetter)
        def insuranceFieldModel = createValidFieldMetaModel("insurance",
            ClassMetaModel.builder()
                .name("insuranceData")
                .fields([
                    personWrapperField
                ])
                .build())

        ClassMetaModel rootObject = ClassMetaModel.builder()
            .name("insuranceWholeData")
            .fields([insuranceFieldModel])
            .build()

        FieldsChainToAssignExpression fieldsChainStrategy = new FieldsChainToAssignExpression(rootObject,
            "argument.getSource()", [insuranceFieldModel, personWrapperField, samplePersonFieldModel, birthDatFieldModel])

        String expectedCode = "Optional.ofNullable(argument.getSource())" +
            wrapWithNextLineWith3Tabs('.map(genericMap -> ((java.util.Map<java.lang.String, java.lang.Object>) genericMap).get("insurance"))') +
            wrapWithNextLineWith3Tabs('.map(genericMap -> ((java.util.Map<java.lang.String, java.lang.Object>) genericMap).get("personWrapper"))') +
            wrapWithNextLineWith3Tabs('.map(value -> InvokableReflectionUtils.getValueOfField(value, "samplePersonDto"))') +
            wrapWithNextLineWith3Tabs('.map(value -> ((pl.jalokim.crudwizard.core.sample.SamplePersonDto) value).getBirthDay())') +
            wrapWithNextLineWith3Tabs('.orElse(null)')

        when:
        def result = fieldsChainStrategy.generateCodeMetadata(mapperGeneratedCodeMetadata)

        then:
        verifyAll(result) {
            returnClassModel == birthDatFieldModel.getFieldType()
            valueGettingCode == expectedCode
        }

        verifyAll(mapperGeneratedCodeMetadata) {
            staticImports.isEmpty()
            imports == ["import ${InvokableReflectionUtils.class.getCanonicalName()};"] as Set
            constructorArguments.isEmpty()
        }
    }
}
