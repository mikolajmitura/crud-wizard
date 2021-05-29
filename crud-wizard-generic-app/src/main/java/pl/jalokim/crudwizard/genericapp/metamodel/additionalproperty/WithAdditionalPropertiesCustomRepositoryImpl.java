package pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import pl.jalokim.crudwizard.genericapp.config.MetaModelSourceJpaConfig;

public class WithAdditionalPropertiesCustomRepositoryImpl<T extends WithAdditionalPropertiesEntity>
    implements WithAdditionalPropertiesCustomRepository<T> {

    @Autowired
    private AdditionalPropertyRepository additionalPropertyRepository;

    @PersistenceContext(unitName = MetaModelSourceJpaConfig.METAMODEL_ENTITY_MANAGER)
    private EntityManager entityManager;

    @Override
    public  T persist(T withAdditionalPropertiesEntity) {
        var newInstance = entityManager.merge(withAdditionalPropertiesEntity);

        withAdditionalPropertiesEntity.getAdditionalProperties()
            .forEach(entry -> {
                    entry.setOwnerClass(withAdditionalPropertiesEntity.getClass().getSimpleName());
                    entry.setOwnerId(newInstance.getId());
                }
            );

        additionalPropertyRepository.saveAll(withAdditionalPropertiesEntity.getAdditionalProperties());
        return newInstance;
    }
}
