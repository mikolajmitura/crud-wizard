package pl.jalokim.crudwizard.genericapp.metamodel.mapper;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.WithAdditionalPropertiesCustomRepository;

@Repository
public interface MapperMetaModelEntityRepository extends JpaRepository<MapperMetaModelEntity, Long>,
    WithAdditionalPropertiesCustomRepository<MapperMetaModelEntity> {

    boolean existsByBeanNameAndClassNameAndMethodName(String beanName, String className, String methodName);
}
