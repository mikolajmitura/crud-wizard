package pl.jalokim.crudwizard.genericapp.metamodel.mapper;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MapperMetaModelEntityRepository extends JpaRepository<MapperMetaModelEntity, Long> {

    @Query("select m from MapperMetaModelEntity m where m.mapperBeanAndMethod.beanName = :beanName " +
        "and m.mapperBeanAndMethod.className = :className " +
        "and m.mapperBeanAndMethod.methodName = :methodName "
    )
    Optional<MapperMetaModelEntity> findByBeanNameAndClassNameAndMethodName(String beanName, String className, String methodName);

    List<MapperMetaModelEntity> findAllByMapperType(MapperType mapperType);
}
