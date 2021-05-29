package pl.jalokim.crudwizard.genericapp.metamodel.endpoint;

import org.springframework.stereotype.Repository;
import pl.jalokim.crudwizard.core.jpa.BaseRepository;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.WithAdditionalPropertiesCustomRepository;

@Repository
public interface EndpointMetaModelRepository extends BaseRepository<EndpointMetaModelEntity>,
    WithAdditionalPropertiesCustomRepository<EndpointMetaModelEntity> {

}
