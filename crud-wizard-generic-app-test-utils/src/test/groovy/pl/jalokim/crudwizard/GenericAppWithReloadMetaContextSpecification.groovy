package pl.jalokim.crudwizard

import org.springframework.beans.factory.annotation.Autowired
import pl.jalokim.crudwizard.genericapp.reload.MetaContextTestLoader

class GenericAppWithReloadMetaContextSpecification extends GenericAppBaseIntegrationSpecification {

    @Autowired
    private MetaContextTestLoader metaContextTestLoader

    def setup() {
        metaContextTestLoader.reload()
    }
}
