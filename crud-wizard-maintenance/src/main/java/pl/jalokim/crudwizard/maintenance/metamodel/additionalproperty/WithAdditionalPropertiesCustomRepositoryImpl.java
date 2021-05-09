package pl.jalokim.crudwizard.maintenance.metamodel.additionalproperty;

import static pl.jalokim.crudwizard.maintenance.config.MaintenanceJpaConfig.MAINTENANCE_ENTITY_MANAGER;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;

public class WithAdditionalPropertiesCustomRepositoryImpl implements WithAdditionalPropertiesCustomRepository {

    @Autowired
    private AdditionalPropertyRepository additionalPropertyRepository;

    @PersistenceContext(unitName = MAINTENANCE_ENTITY_MANAGER)
    private EntityManager entityManager;

    @Override
    public <T extends WithAdditionalPropertiesEntity> T persist(T withAdditionalPropertiesEntity) {
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
