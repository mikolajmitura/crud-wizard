package pl.jalokim.crudwizard.examples.simpleapp

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestExecutionListeners
import pl.jalokim.crudwizard.SimpleApplicationTestConfig
import pl.jalokim.crudwizard.test.utils.BaseIntegrationSpecification
import pl.jalokim.crudwizard.test.utils.cleaner.DatabaseCleanupListener

@ActiveProfiles("integration")
@SpringBootTest(classes = [SimpleApplicationTestConfig], webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestExecutionListeners(value = [DatabaseCleanupListener], mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class SimpleApplicationMainIT extends BaseIntegrationSpecification {

    // TODO test in future that was added some endpoint via maitenance and can be invoked

    def "should load data from SimpleRestController.getAll()"() {
        when:
        def response = operationsOnRestController.getAndReturnArrayJson("/simple/users")

        then:
        response == ["user1", "user2"]
    }
}
