package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModelService;

@Component
@RequiredArgsConstructor
public class ClassMetaModelEntitySaveContext {

    private final ThreadLocal<SavedEntities> alreadySavedClassMetaModelByName = new ThreadLocal<>();
    private final ValidatorMetaModelService validatorMetaModelService;
    private final ClassMetaModelRepository classMetaModelRepository;
    private final ClassMetaModelMapper classMetaModelMapper;

    public void clearSaveContext() {
        alreadySavedClassMetaModelByName.set(null);
    }

    public void setupContext() {
        alreadySavedClassMetaModelByName.set(new SavedEntities(
            validatorMetaModelService,
            classMetaModelRepository,
            classMetaModelMapper));
    }

    public SavedEntities getSavedEntities() {
        return alreadySavedClassMetaModelByName.get();
    }

    public ClassMetaModelEntity findFullySavedWhenNameTheSame(ClassMetaModelEntity classMetaModelEntity) {
        return getSavedEntities().findFullySavedWhenNameTheSame(classMetaModelEntity);
    }

    public ClassMetaModelEntity findPartiallySavedWhenNameTheSame(ClassMetaModelEntity classMetaModelEntity) {
        return getSavedEntities().findPartiallySavedWhenNameTheSame(classMetaModelEntity);
    }

    public void putFullySavedToContext(ClassMetaModelEntity savedClassMetaModelEntity) {
        getSavedEntities().putFullySavedToContext(savedClassMetaModelEntity);
    }

    public void putPartiallySavedToContext(ClassMetaModelDto classMetaModelDto) {
        getSavedEntities().putPartiallySavedToContext(classMetaModelDto);
    }

    @RequiredArgsConstructor
    public static class SavedEntities {

        private final ValidatorMetaModelService validatorMetaModelService;
        private final ClassMetaModelRepository classMetaModelRepository;
        private final ClassMetaModelMapper classMetaModelMapper;
        private final Map<String, ClassMetaModelEntity> partiallyEntitiesSaved = new HashMap<>();
        private final Map<String, ClassMetaModelEntity> fullyEntitiesSaved = new HashMap<>();

        public void putFullySavedToContext(ClassMetaModelEntity savedClassMetaModelEntity) {
            if (savedClassMetaModelEntity.getName() != null) {
                Objects.requireNonNull(savedClassMetaModelEntity.getId(), "entity should have assigned id already");
                fullyEntitiesSaved.put(savedClassMetaModelEntity.getName(), savedClassMetaModelEntity);
            }
        }

        public void putPartiallySavedToContext(ClassMetaModelDto classMetaModelDto) {

            if (classMetaModelDto.getName() != null && !partiallyEntitiesSaved.containsKey(classMetaModelDto.getName())) {
                ClassMetaModelEntity classMetaModelEntity = classMetaModelMapper.toSimpleEntity(classMetaModelDto, true);
                validatorMetaModelService.saveOrCreateNewValidators(classMetaModelEntity.getValidators());
                ClassMetaModelEntity savedClassMetaModelEntity = classMetaModelRepository.save(classMetaModelEntity);
                partiallyEntitiesSaved.put(savedClassMetaModelEntity.getName(), savedClassMetaModelEntity);
            }
        }

        public ClassMetaModelEntity findFullySavedWhenNameTheSame(ClassMetaModelEntity classMetaModelEntity) {
            if (classMetaModelEntity.getName() != null) {
                ClassMetaModelEntity currentClassMetaModelEntityInContext = fullyEntitiesSaved.get(classMetaModelEntity.getName());
                if (currentClassMetaModelEntityInContext != null) {
                    return currentClassMetaModelEntityInContext;
                }
            }
            return classMetaModelEntity;
        }

        public ClassMetaModelEntity findPartiallySavedWhenNameTheSame(ClassMetaModelEntity classMetaModelEntity) {
            if (classMetaModelEntity.getName() != null) {
                ClassMetaModelEntity currentClassMetaModelEntityInContext = partiallyEntitiesSaved.get(classMetaModelEntity.getName());
                if (currentClassMetaModelEntityInContext != null) {
                    return currentClassMetaModelEntityInContext;
                }
            }
            return classMetaModelEntity;
        }
    }
}
