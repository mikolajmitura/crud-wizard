package pl.jalokim.crudwizard.test.utils.cleaner

import org.springframework.beans.factory.annotation.Autowired
import pl.jalokim.crudwizard.test.utils.DummyBaseIntegrationControllerSpec
import spock.lang.Unroll

class DatabaseCleanupListenerWithoutCleanupOnceTest extends DummyBaseIntegrationControllerSpec {

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
    def "should find #expectedSize entries from entries from created before all test (test 1) for: #invocationIndex index"() {
        when:
        def currentSize = dummyRepository1.findAll().size()
        then:
        currentSize == expectedSize || currentSize == 0
        where:
        invocationIndex || expectedSize
        1               || 5
        2               || 0
        3               || 0
    }

    @Unroll
    def "should find #expectedSize entries from created before all test (test 2) for: #invocationIndex index"() {
        given:
        executeOnlyOnce {
            (1..4).forEach {
                DummyEntity entity1 = new DummyEntity()
                entity1.setName("name:${it}")
                dummyRepository1.save(entity1)
            }
        }
        when:
        def currentSize = dummyRepository1.findAll().size()
        then:
        currentSize == expectedSize || currentSize == expectedSize + 5
        where:
        invocationIndex || expectedSize
        1               || 4
        2               || 0
        3               || 0
    }
}
