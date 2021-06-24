package pl.jalokim.crudwizard

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestExecutionListeners
import pl.jalokim.crudwizard.test.utils.BaseIntegrationSpecification
import pl.jalokim.crudwizard.test.utils.cleaner.DatabaseCleanupListener

@ActiveProfiles(["integration"])
@SpringBootTest(classes = [TestsApplicationConfig], webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestExecutionListeners(value = [DatabaseCleanupListener], mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class GenericAppBaseIntegrationSpecification extends BaseIntegrationSpecification {
}
