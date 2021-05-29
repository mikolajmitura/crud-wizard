package pl.jalokim.crudwizard.maintenance.metamodel.endpoint

import org.springframework.stereotype.Component
import pl.jalokim.crudwizard.test.utils.BaseOperationsOnRestController

@Component
class OperationsOnEndpointRestController extends BaseOperationsOnRestController {

    @Override
    String getEndpointUrl() {
        "/maintenance/endpoints"
    }
}
