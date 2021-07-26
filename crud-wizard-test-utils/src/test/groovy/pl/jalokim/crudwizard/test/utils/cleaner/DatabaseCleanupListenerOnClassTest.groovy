package pl.jalokim.crudwizard.test.utils.cleaner

import org.springframework.beans.factory.annotation.Autowired
import pl.jalokim.crudwizard.test.utils.DummyBaseIntegrationControllerSpec
import spock.lang.Unroll

@CleanupOnce
class DatabaseCleanupListenerOnClassTest extends DummyBaseIntegrationControllerSpec {

    @Autowired
    private DummyRepository dummyRepository1

    def setup() {
        executeOnlyOnce {
            (1..5).forEach {
                DummyEntity entity1 = new DummyEntity()
                entity1.setName("name:${it}")
                dummyRepository1.save(entity1)
            }
        }
    }

    @Unroll
    def "should find 5 entries from created before all test (test 1) for: #invocationIndex index"() {
        when:
        def currentSize = dummyRepository1.findAll().size()
        then:
        currentSize == 5
        where:
        invocationIndex || _
        1               || _
        2               || _
        3               || _
    }

    @Unroll
    def "should find 5 entries from created before all test (test 2) for: #invocationIndex index"() {
        when:
        def currentSize = dummyRepository1.findAll().size()
        then:
        currentSize == 5
        where:
        invocationIndex || _
        1               || _
        2               || _
        3               || _
    }
}
