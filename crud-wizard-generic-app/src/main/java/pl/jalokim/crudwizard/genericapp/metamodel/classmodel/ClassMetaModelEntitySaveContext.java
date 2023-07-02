package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelsUtils.isClearRawClassFullDefinition;
import static pl.jalokim.utils.collection.CollectionUtils.isEmpty;
import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.string.StringUtils.isNotBlank;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelsUtils;
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

    public ClassMetaModelEntity findFullySaved(ClassMetaModelEntity classMetaModelEntity) {
        return getSavedEntities().findFullySaved(classMetaModelEntity);
    }

    public ClassMetaModelEntity findPartiallySaved(ClassMetaModelEntity classMetaModelEntity) {
        return getSavedEntities().findPartiallySaved(classMetaModelEntity);
    }

    public void putFullySavedToContext(ClassMetaModelEntity savedClassMetaModelEntity) {
        getSavedEntities().putFullySavedToContext(savedClassMetaModelEntity);
    }

    public void putPartiallySavedToContext(ClassMetaModelDto classMetaModelDto) {
        getSavedEntities().putPartiallySavedToContext(classMetaModelDto);
    }

    public void putDuringInitializationEntity(ClassMetaModelEntity classMetaModelEntity) {
        getSavedEntities().putDuringInitializationEntity(classMetaModelEntity);
    }

    public boolean isDuringFullSave(ClassMetaModelEntity classMetaModelEntity) {
        return getSavedEntities().isDuringFullSave(classMetaModelEntity);
    }

    @RequiredArgsConstructor
    public static class SavedEntities {

        private final ValidatorMetaModelService validatorMetaModelService;
        private final ClassMetaModelRepository classMetaModelRepository;
        private final ClassMetaModelMapper classMetaModelMapper;
        private final Map<ClassKey, ClassMetaModelEntity> partiallyEntitiesSaved = new HashMap<>();
        private final Map<ClassKey, ClassMetaModelEntity> duringFullSaveEntities = new HashMap<>();
        private final Map<ClassKey, ClassMetaModelEntity> fullyEntitiesSaved = new HashMap<>();

        public void putFullySavedToContext(ClassMetaModelEntity savedClassMetaModelEntity) {
            if (canBeSaved(savedClassMetaModelEntity)) {
                Objects.requireNonNull(savedClassMetaModelEntity.getId(), "entity should have assigned id already");
                fullyEntitiesSaved.put(createKey(savedClassMetaModelEntity), savedClassMetaModelEntity);
            }
        }

        public void putPartiallySavedToContext(ClassMetaModelDto classMetaModelDto) {
            if (canBeSaved(classMetaModelDto) && !partiallyEntitiesSaved.containsKey(createKey(classMetaModelDto))) {
                ClassMetaModelEntity savedClassMetaModelEntity = null;
                if (isNotBlank(classMetaModelDto.getClassName())) {
                    List<ClassMetaModelEntity> foundClasses = classMetaModelRepository.findByClassName(classMetaModelDto.getClassName());
                    for (ClassMetaModelEntity foundClass : foundClasses) {
                        if (ClassMetaModelsUtils.isClearRawClassFullDefinition(foundClass)) {
                            savedClassMetaModelEntity = foundClass;
                            if (isNotEmpty(classMetaModelDto.getFields()) && CollectionUtils.isEmpty(foundClass.getFields())) {
                                partiallyEntitiesSaved.put(createKey(classMetaModelDto), savedClassMetaModelEntity);
                            } else {
                                fullyEntitiesSaved.put(createKey(classMetaModelDto), savedClassMetaModelEntity);
                            }
                        }
                    }
                }

                if (savedClassMetaModelEntity == null) {
                    ClassMetaModelEntity classMetaModelEntity = classMetaModelMapper.toSimpleEntity(classMetaModelDto, false);
                    validatorMetaModelService.saveOrCreateNewValidators(classMetaModelEntity.getValidators());
                    if (shouldBeSimpleRawClass(classMetaModelDto)) {
                        classMetaModelEntity.setSimpleRawClass(true);
                    }
                    savedClassMetaModelEntity = classMetaModelRepository.save(classMetaModelEntity);
                    partiallyEntitiesSaved.put(createKey(classMetaModelDto), savedClassMetaModelEntity);
                }
            }
        }

        public ClassMetaModelEntity findFullySaved(ClassMetaModelEntity classMetaModelEntity) {
            if (canBeSaved(classMetaModelEntity)) {
                ClassMetaModelEntity currentClassMetaModelEntityInContext = fullyEntitiesSaved.get(createKey(classMetaModelEntity));
                if (currentClassMetaModelEntityInContext != null) {
                    return currentClassMetaModelEntityInContext;
                }
            }
            return classMetaModelEntity;
        }

        public ClassMetaModelEntity findPartiallySaved(ClassMetaModelEntity classMetaModelEntity) {
            if (canBeSaved(classMetaModelEntity)) {
                ClassMetaModelEntity currentClassMetaModelEntityInContext = partiallyEntitiesSaved.get(createKey(classMetaModelEntity));
                if (currentClassMetaModelEntityInContext != null) {
                    return currentClassMetaModelEntityInContext;
                }
            }
            return classMetaModelEntity;
        }

        public void putDuringInitializationEntity(ClassMetaModelEntity classMetaModelEntity) {
            if (canBeSaved(classMetaModelEntity) && !duringFullSaveEntities.containsKey(createKey(classMetaModelEntity))) {
                duringFullSaveEntities.put(createKey(classMetaModelEntity), classMetaModelEntity);
            }
        }

        public boolean isDuringFullSave(ClassMetaModelEntity classMetaModelEntity) {
            return canBeSaved(classMetaModelEntity) && duringFullSaveEntities.containsKey(createKey(classMetaModelEntity));
        }
    }

    private static boolean canBeSaved(ClassMetaModelDto classMetaModelDto) {
        return createKey(classMetaModelDto) != null;
    }

    private static ClassKey createKey(ClassMetaModelDto classMetaModelDto) {
        if (isNotBlank(classMetaModelDto.getName())) {
            return new ClassKey("n: " + classMetaModelDto.getName());
        } else if (isClearRawClassFullDefinition(classMetaModelDto)) {
            return new ClassKey("c: " + classMetaModelDto.getClassName());
        }
        return null;
    }

    private static boolean canBeSaved(ClassMetaModelEntity classMetaModelEntity) {
        return createKey(classMetaModelEntity) != null;
    }

    private static ClassKey createKey(ClassMetaModelEntity classMetaModelEntity) {
        if (isNotBlank(classMetaModelEntity.getName())) {
            return new ClassKey("n: " + classMetaModelEntity.getName());
        } else if (isClearRawClassFullDefinition(classMetaModelEntity)) {
            return new ClassKey("c: " + classMetaModelEntity.getClassName());
        }
        return null;
    }

    @Value
    private static class ClassKey {

        String value;
    }

    private static boolean shouldBeSimpleRawClass(ClassMetaModelDto classMetaModelDto) {
        return isNotBlank(classMetaModelDto.getClassName()) &&
            isEmpty(elements(classMetaModelDto.getFields())) &&
            isEmpty(elements(classMetaModelDto.getGenericTypes())) &&
            isEmpty(elements(classMetaModelDto.getExtendsFromModels())) &&
            isEmpty(elements(classMetaModelDto.getValidators()));
    }
}
