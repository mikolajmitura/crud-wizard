package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import static pl.jalokim.crudwizard.core.translations.AppMessageSourceHolder.getMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.exception.TechnicalException;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.AdditionalPropertyEntity;
import pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModelEntity;

@Component
public class ClassMetaModelEntitySaveContext {

    private final ThreadLocal<Map<String, ClassMetaModelEntity>> alreadySavedClassMetaModelByName = new ThreadLocal<>();

    public void clearSaveContext() {
        alreadySavedClassMetaModelByName.set(null);
    }

    public void setupContext() {
        alreadySavedClassMetaModelByName.set(new HashMap<>());
    }

    public ClassMetaModelEntity findEntityWhenNameTheSame(ClassMetaModelEntity classMetaModelEntity) {
        if (classMetaModelEntity.getName() != null) {
            ClassMetaModelEntity currentClassMetaModelEntityInContext = alreadySavedClassMetaModelByName.get().get(classMetaModelEntity.getName());
            if (currentClassMetaModelEntityInContext != null) {
                validateClassMetaModelContents(classMetaModelEntity, currentClassMetaModelEntityInContext);
                return currentClassMetaModelEntityInContext;
            }
        }
        return classMetaModelEntity;
    }

    private void validateClassMetaModelContents(ClassMetaModelEntity classMetaModelEntity, ClassMetaModelEntity currentClassMetaModelEntityInContext) {
        if (!theSameContent(classMetaModelEntity, currentClassMetaModelEntityInContext)) {
            throw new TechnicalException(getMessage("class.metamodel.the.same.name.but.other.content",
                classMetaModelEntity.getName()));
        }
    }

    public void putToContext(ClassMetaModelEntity savedClassMetaModelEntity) {
        if (savedClassMetaModelEntity.getName() != null) {
            alreadySavedClassMetaModelByName.get().put(savedClassMetaModelEntity.getName(), savedClassMetaModelEntity);
        }
    }

    public boolean theSameContent(ClassMetaModelEntity leftClassModel, ClassMetaModelEntity rightClassModel) {
        return areTheSameClassMetaModels(List.of(leftClassModel), List.of(rightClassModel));
    }

    private boolean areTheSameClassMetaModels(List<ClassMetaModelEntity> left, List<ClassMetaModelEntity> right) {
        return listsAreTheSame(left, right,
            List.of(
                ClassMetaModelEntity::getName,
                ClassMetaModelEntity::getClassName,
                ClassMetaModelEntity::getIsGenericEnumType,
                ClassMetaModelEntity::getSimpleRawClass),
            (leftClassModel, rightClassModel) ->
                areTheSameClassMetaModels(leftClassModel.getGenericTypes(), rightClassModel.getGenericTypes()),
            (leftClassModel, rightClassModel) ->
                listsAreTheSame(leftClassModel.getFields(), rightClassModel.getFields(),
                    List.of(FieldMetaModelEntity::getFieldName),
                    (leftFieldClassModel, rightFieldClassModel) ->
                        areTheSameValidators(leftFieldClassModel.getValidators(), rightFieldClassModel.getValidators()),
                    (leftFieldClassModel, rightFieldClassModel) ->
                        areTheSameAdditionalProperties(leftFieldClassModel.getAdditionalProperties(), rightFieldClassModel.getAdditionalProperties())),
            (leftClassModel, rightClassModel) ->
                areTheSameValidators(leftClassModel.getValidators(), rightClassModel.getValidators()),
            (leftClassModel, rightClassModel) ->
                areTheSameClassMetaModels(leftClassModel.getExtendsFromModels(), rightClassModel.getExtendsFromModels()),
            (leftClassModel, rightClassModel) ->
                areTheSameAdditionalProperties(leftClassModel.getAdditionalProperties(), rightClassModel.getAdditionalProperties())
        );
    }

    private boolean areTheSameValidators(List<ValidatorMetaModelEntity> left, List<ValidatorMetaModelEntity> right) {
        return listsAreTheSame(left, right,
            List.of(
                ValidatorMetaModelEntity::getClassName,
                ValidatorMetaModelEntity::getValidatorName,
                ValidatorMetaModelEntity::getValidatorScript,
                ValidatorMetaModelEntity::getParametrized,
                ValidatorMetaModelEntity::getNamePlaceholder,
                ValidatorMetaModelEntity::getMessagePlaceholder),
            (leftValidator, rightValidator) ->
                areTheSameAdditionalProperties(leftValidator.getAdditionalProperties(), rightValidator.getAdditionalProperties()));
    }

    private boolean areTheSameAdditionalProperties(List<AdditionalPropertyEntity> left, List<AdditionalPropertyEntity> right) {
        return listsAreTheSame(left, right,
            List.of(
                AdditionalPropertyEntity::getName,
                AdditionalPropertyEntity::getValueRealClassName,
                AdditionalPropertyEntity::getRawJson
            ));
    }

    @SafeVarargs
    private <T> boolean listsAreTheSame(List<T> left, List<T> right, List<Function<T, Object>> getters,
        BiPredicate<T, T>... predicates) {
        if (left == null && right == null) {
            return true;
        }

        if (left != null && right != null) {
            if (left.size() != right.size()) {
                return false;
            }

            boolean areEquals = true;

            for (int index = 0; index < left.size(); index++) {
                T leftElement = left.get(index);
                T rightElement = right.get(index);
                areEquals = valuesByFieldsAreTheSame(areEquals, getters, leftElement, rightElement);

                for (BiPredicate<T, T> predicate : predicates) {
                    areEquals = areEquals && predicate.test(leftElement, rightElement);
                }

            }
            return areEquals;
        }

        return false;
    }

    private <T> boolean valuesByFieldsAreTheSame(boolean areEquals, List<Function<T, Object>> getters, T leftElement, T rightElement) {
        return areEquals && getters.stream()
            .allMatch(getter -> Objects.equals(getter.apply(leftElement), getter.apply(rightElement)));
    }
}
