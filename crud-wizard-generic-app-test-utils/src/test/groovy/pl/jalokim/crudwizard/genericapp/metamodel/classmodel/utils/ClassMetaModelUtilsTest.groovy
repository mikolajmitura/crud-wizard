package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils

import static pl.jalokim.crudwizard.core.metamodels.ClassMetaModelSamples.createClassModelWithGenerics

import com.fasterxml.jackson.databind.ObjectMapper
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel
import pl.jalokim.crudwizard.genericapp.service.translator.JsonObjectMapper
import spock.lang.Specification

class ClassMetaModelUtilsTest extends Specification {

    def setup() {
        def jsonObjectMapper = new JsonObjectMapper(new ObjectMapper())
        jsonObjectMapper.postConstruct()
    }

    def "should return jackson JavaType as expected"() {
        given:
        ClassMetaModel classMetaModel = createClassModelWithGenerics(List, String)

        when:
        def result = ClassMetaModelUtils.createJacksonJavaType(classMetaModel)

        then:
        result.rawClass == List
        result.getBindings().getTypeParameters()[0].getRawClass() == String
    }
}
