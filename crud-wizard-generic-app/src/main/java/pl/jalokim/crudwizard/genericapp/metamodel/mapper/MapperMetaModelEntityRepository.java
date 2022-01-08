package pl.jalokim.crudwizard.genericapp.metamodel.mapper;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MapperMetaModelEntityRepository extends JpaRepository<MapperMetaModelEntity, Long> {

    boolean existsByBeanNameAndClassNameAndMethodName(String beanName, String className, String methodName);
}
