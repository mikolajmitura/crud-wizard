package pl.jalokim.crudwizard.maintenance.metamodel.endpoint

import org.springframework.stereotype.Component
import pl.jalokim.crudwizard.test.utils.BaseOperationsOnEndpoints

@Component
@SuppressWarnings("GetterMethodCouldBeProperty")
class OperationsOnEndpointRestController extends BaseOperationsOnEndpoints {

    @Override
    String getEndpointUrl() {
        "/maintenance/endpoints"
    }
}
