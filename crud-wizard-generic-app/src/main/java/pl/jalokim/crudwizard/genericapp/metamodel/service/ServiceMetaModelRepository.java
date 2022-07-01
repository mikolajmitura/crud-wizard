package pl.jalokim.crudwizard.genericapp.metamodel.service;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.jalokim.crudwizard.core.jpa.BaseRepository;

@Repository
public interface ServiceMetaModelRepository extends BaseRepository<ServiceMetaModelEntity>  {

    @Query("select count(s) > 0 from ServiceMetaModelEntity s where s.serviceBeanAndMethod.beanName = :beanName "
        + "and s.serviceBeanAndMethod.className = :className "
        + "and s.serviceBeanAndMethod.methodName = :methodName "
    )
    boolean existsByBeanNameAndClassNameAndMethodName(String beanName, String className, String methodName);
}
