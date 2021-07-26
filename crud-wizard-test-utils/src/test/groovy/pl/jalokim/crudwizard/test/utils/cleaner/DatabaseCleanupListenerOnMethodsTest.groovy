package pl.jalokim.crudwizard.test.utils.cleaner

import org.springframework.beans.factory.annotation.Autowired
import pl.jalokim.crudwizard.test.utils.DummyBaseIntegrationControllerSpec
import spock.lang.Shared
import spock.lang.Unroll

class DatabaseCleanupListenerOnMethodsTest extends DummyBaseIntegrationControllerSpec {

    @Autowired
    private DummyRepository dummyRepository1

    @Shared
    List<Long> created1Ids = []

    @Shared
    List<Long> created2Ids = []

    @Shared
    List<Long> created3Ids = []

    @Unroll
    @CleanupOnce
    def "should always return the 5 number of of dummy1 entities for: #invocationIndex"() {
        given:
        executeOnlyOnce {
            (1..5).forEach {
                DummyEntity entity1 = new DummyEntity()
                entity1.setName("name:${it}")
                created1Ids << dummyRepository1.save(entity1).getId()
            }
        }
        when:
        def numberOrEntities = dummyRepository1.findAll().size()

        then:
        created1Ids.size() == 5
        created1Ids as Set == dummyRepository1.findAll()*.id as Set
        numberOrEntities == 5

        where:
        invocationIndex || _
        1               || _
        2               || _
        3               || _
        4               || _
    }

    @Unroll
    @CleanupOnce
    def "should always return the 4 number of of dummy1 entities for: #invocationIndex"() {
        given:
        executeOnlyOnce {
            (1..4).forEach {
                DummyEntity entity1 = new DummyEntity()
                entity1.setName("name:${it}")
                created2Ids << dummyRepository1.save(entity1).getId()
            }
        }
        when:
        def numberOrEntities = dummyRepository1.findAll().size()

        then:
        created2Ids.size() == 4
        created2Ids as Set == dummyRepository1.findAll()*.id as Set
        numberOrEntities == 4

        where:
        invocationIndex || _
        1               || _
        2               || _
        3               || _
    }

    @Unroll
    def "should return the #expectedSize number of of dummy1 entities for: #invocationIndex"() {
        given:
        executeOnlyOnce {
            (1..4).forEach {
                DummyEntity entity1 = new DummyEntity()
                entity1.setName("name:${it}")
                created3Ids << dummyRepository1.save(entity1).getId()
            }
        }
        when:
        def numberOrEntities = dummyRepository1.findAll().size()

        then:
        created3Ids.size() == 4
        numberOrEntities == expectedSize

        where:
        invocationIndex || expectedSize
        1               || 4
        2               || 0
        3               || 0
    }
}
