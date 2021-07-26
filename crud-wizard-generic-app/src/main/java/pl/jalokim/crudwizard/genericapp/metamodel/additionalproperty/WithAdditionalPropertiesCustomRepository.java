package pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty;

import org.springframework.stereotype.Repository;

@Repository
public interface WithAdditionalPropertiesCustomRepository<T extends WithAdditionalPropertiesEntity> {

    T persist(T withAdditionalPropertiesEntity);
}
