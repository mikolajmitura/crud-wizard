package pl.jalokim.crudwizard.genericapp.metamodel.service;

import org.springframework.stereotype.Repository;
import pl.jalokim.crudwizard.core.jpa.BaseRepository;

@Repository
public interface ServiceMetaModelRepository extends BaseRepository<ServiceMetaModelEntity>  {

    boolean existsByBeanNameAndClassNameAndMethodName(String beanName, String className, String methodName);
}
