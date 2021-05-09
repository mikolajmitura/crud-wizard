package pl.jalokim.crudwizard.maintenance.metamodel.additionalproperty;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import pl.jalokim.crudwizard.core.annotations.MaintenanceService;

@MaintenanceService
@AllArgsConstructor
public class AdditionalPropertyService {

    private static final AtomicReference<AdditionalPropertyService> INSTANCE = new AtomicReference<>();

    private final AdditionalPropertyRepository additionalPropertyRepository;

    @PostConstruct
    public void postConstruct() {
        INSTANCE.set(this);
    }

    public List<AdditionalPropertyEntity> fetchAdditionalPropertiesFor(WithAdditionalPropertiesEntity withAdditionalPropertiesEntity) {
        return additionalPropertyRepository.findByOwnerIdAndOwnerClass(
            withAdditionalPropertiesEntity.getId(),
            withAdditionalPropertiesEntity.getClass().getName());
    }

    public static AdditionalPropertyService getInstance() {
        return INSTANCE.get();
    }
}
