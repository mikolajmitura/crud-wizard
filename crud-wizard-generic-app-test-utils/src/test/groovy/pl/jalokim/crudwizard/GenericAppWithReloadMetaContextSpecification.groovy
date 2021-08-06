package pl.jalokim.crudwizard

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextRefreshEvent

class GenericAppWithReloadMetaContextSpecification extends GenericAppBaseIntegrationSpecification {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher

    def setup() {
        applicationEventPublisher.publishEvent(new MetaModelContextRefreshEvent("reload for test", timeProvider.getCurrentOffsetDateTime()))
    }
}
