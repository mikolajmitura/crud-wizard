package pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue

import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createValidFieldMetaModel
import static pl.jalokim.crudwizard.genericapp.mapper.generete.GeneratedLineUtils.wrapWithNextLineWith3Tabs

import java.time.LocalDate
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel
import pl.jalokim.crudwizard.core.sample.SamplePersonDto
import pl.jalokim.crudwizard.genericapp.rest.samples.dto.SampleDtoWithoutGetter
import pl.jalokim.utils.reflection.InvokableReflectionUtils
import spock.lang.Specification

class FromFieldsChainStrategyTest extends Specification {

    def "return expected method mapping chain"() {
        given:
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

        FromFieldsChainStrategy fieldsChainStrategy = new FromFieldsChainStrategy(rootObject,
            "argument.getSource()", [insuranceFieldModel, personWrapperField, samplePersonFieldModel, birthDatFieldModel])

        String expectedCode = "Optional.ofNullable(argument.getSource())" +
            wrapWithNextLineWith3Tabs('.map(genericMap -> ((Map<String, Object>) genericMap).get("insurance"))') +
            wrapWithNextLineWith3Tabs('.map(genericMap -> ((Map<String, Object>) genericMap).get("personWrapper"))') +
            wrapWithNextLineWith3Tabs('.map(value -> InvokableReflectionUtils.getValueOfField(value, "samplePersonDto"))') +
            wrapWithNextLineWith3Tabs('.map(value -> ((pl.jalokim.crudwizard.core.sample.SamplePersonDto) value).getBirthDay())') +
            wrapWithNextLineWith3Tabs('.orElse(null)')

        when:
        def result = fieldsChainStrategy.generateReturnCodeMetadata()

        then:
        verifyAll(result) {
            staticImports.isEmpty()
            imports == ["import ${InvokableReflectionUtils.class.getCanonicalName()};"] as Set
            returnClassModel == birthDatFieldModel.getFieldType()
            valueGettingCode == expectedCode
            constructorArguments.isEmpty()
        }
    }
}
