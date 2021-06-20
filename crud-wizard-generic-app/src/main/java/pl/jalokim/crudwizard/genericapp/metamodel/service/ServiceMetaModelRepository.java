package pl.jalokim.crudwizard.genericapp.metamodel.service;

import org.springframework.stereotype.Repository;
import pl.jalokim.crudwizard.core.jpa.BaseRepository;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.WithAdditionalPropertiesCustomRepository;

@Repository
public interface ServiceMetaModelRepository extends BaseRepository<ServiceMetaModelEntity>,
    WithAdditionalPropertiesCustomRepository<ServiceMetaModelEntity> {

    boolean existsByBeanNameAndClassNameAndMethodName(String beanName, String className, String methodName);
}
