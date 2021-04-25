package pl.jalokim.crudwizard.test.utils

class BaseIntegrationSpecificationTest extends BaseIntegrationSpecification {

    int invokedClosure1
    int invokedClosure2
    int invokedClosure3

    def "executeOnlyOnce should be invoked only one per every closure"() {
        when:
        (1..5).forEach({
            executeOnlyOnce {
                invokedClosure1++
            }
            executeOnlyOnce {
                invokedClosure2++
            }
            executeOnlyOnce {
                invokedClosure3++
            }
        })
        then:
        invokedClosure1 == 1
        invokedClosure2 == 1
        invokedClosure3 == 1
    }
}
