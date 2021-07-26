package pl.jalokim.crudwizard.genericapp.metamodel.context

import static pl.jalokim.crudwizard.genericapp.metamodel.apitag.ApiTagEntitySamples.sampleApiTagEntity
import static pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelDtoSamples.createValidPostEndpointMetaModelDto

import org.springframework.beans.factory.annotation.Autowired
import pl.jalokim.crudwizard.GenericAppBaseIntegrationSpecification
import pl.jalokim.crudwizard.genericapp.metamodel.apitag.ApiTagRepository
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.EndpointMetaModelService

class MetaModelContextServiceIT extends GenericAppBaseIntegrationSpecification {

    @Autowired
    private ApiTagRepository apiTagRepository

    @Autowired
    private MetaModelContextService metaModelContextService

    @Autowired
    private EndpointMetaModelService endpointMetaModelService

    // TODO #3 to implement load context
    // TODO #4 to implement load context with urls hierarchy etc
    @SuppressWarnings(["UnusedVariable"])
    def "should load all meta models as expected for simple case, default mapper, default service, default data storage"() {
        given:
        def firstApiTag = apiTagRepository.save(sampleApiTagEntity())
        def secondApiTag = apiTagRepository.save(sampleApiTagEntity())
        def endpointId = endpointMetaModelService.createNewEndpoint(createValidPostEndpointMetaModelDto())

        when:
        metaModelContextService.reloadAll()
        def reloadedContext = metaModelContextService.getMetaModelContext()

        then:
        verifyAll(reloadedContext) {}
    }
}
