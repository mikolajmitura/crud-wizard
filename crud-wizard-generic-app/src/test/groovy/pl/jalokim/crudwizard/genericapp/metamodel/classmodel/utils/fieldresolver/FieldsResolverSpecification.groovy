package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver

import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModel
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelFactory
import pl.jalokim.crudwizard.test.utils.UnitTestSpec

class FieldsResolverSpecification extends UnitTestSpec {

    def setup() {
        ClassMetaModelFactory.clearCache()
    }

    FieldMetaModel findField(Collection<FieldMetaModel> results, String fieldName) {
        results.find {
            it.fieldName == fieldName
        }
    }
}
