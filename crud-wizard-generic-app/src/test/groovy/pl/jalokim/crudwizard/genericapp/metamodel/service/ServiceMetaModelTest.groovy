package pl.jalokim.crudwizard.genericapp.metamodel.service

import pl.jalokim.crudwizard.core.metamodels.ServiceMetaModel
import spock.lang.Specification

class ServiceMetaModelTest extends Specification {

    def "is generic service meta mode expected: #expected"() {
        when:
        def isGeneric = serviceMetaModel.isGenericServiceMetaModel()

        then:
        isGeneric == expected

        where:
        serviceMetaModel                   || expected
        ServiceMetaModel.builder().build() || true
        ServiceMetaModel.builder()
            .id(12)
            .realMethodName("name")
            .build()                       || false
    }
}
