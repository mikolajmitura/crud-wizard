package pl.jalokim.crudwizard.maintenance

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestExecutionListeners
import pl.jalokim.crudwizard.genericapp.reload.MetaContextTestLoader
import pl.jalokim.crudwizard.test.utils.BaseIntegrationSpecification
import pl.jalokim.crudwizard.test.utils.cleaner.DatabaseCleanupListener

@ActiveProfiles("integration")
@SpringBootTest(classes = [MaintenanceTestsApplicationConfig], webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestExecutionListeners(value = [DatabaseCleanupListener], mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
class MaintenanceBaseIntegrationController extends BaseIntegrationSpecification {

    @Autowired
    private MetaContextTestLoader metaContextTestLoader

    def setup() {
        metaContextTestLoader.reload()
    }
}
