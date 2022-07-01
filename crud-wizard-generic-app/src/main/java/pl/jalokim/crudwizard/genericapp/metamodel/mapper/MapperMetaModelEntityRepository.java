package pl.jalokim.crudwizard.genericapp.metamodel.mapper;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MapperMetaModelEntityRepository extends JpaRepository<MapperMetaModelEntity, Long> {

    @Query("select count(m) > 0 from MapperMetaModelEntity m where m.mapperBeanAndMethod.beanName = :beanName "
        + "and m.mapperBeanAndMethod.className = :className "
        + "and m.mapperBeanAndMethod.methodName = :methodName "
    )
    boolean existsByBeanNameAndClassNameAndMethodName(String beanName, String className, String methodName);
}
