package pl.jalokim.crudwizard.genericapp.metamodel.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.validation.ValidatorMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.url.PropertyPath;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdditionalValidatorsMetaModel {

    private Map<String, AdditionalValidatorsMetaModel> validatorsByPropertyPath;
    private PropertyPath propertyPath;
    private List<ValidatorMetaModel> validatorsMetaModel;

    public static AdditionalValidatorsMetaModel empty() {
        return AdditionalValidatorsMetaModel.builder()
            .validatorsMetaModel(new ArrayList<>())
            .propertyPath(PropertyPath.createRoot())
            .validatorsByPropertyPath(new ConcurrentHashMap<>())
            .build();
    }

    public AdditionalValidatorsMetaModel getOrCreateNextNode(PropertyPath propertyPath) {
        return validatorsByPropertyPath.computeIfAbsent(propertyPath.getPathValue(), (key) -> {
            AdditionalValidatorsMetaModel newNode = empty();
            newNode.setPropertyPath(propertyPath);
            return newNode;
        });
    }

    public AdditionalValidatorsMetaModel getByPropertyPath(PropertyPath propertyPath) {
        AdditionalValidatorsMetaModel additionalValidators;
        if (propertyPath.isArrayElement()) {
            additionalValidators = Optional.ofNullable(validatorsByPropertyPath.get(propertyPath.getPathValue()))
                .orElseGet(() -> validatorsByPropertyPath.get(PropertyPath.ALL_INDEXES));
        } else {
            additionalValidators = validatorsByPropertyPath.get(propertyPath.getPathValue());
        }

        return Optional.ofNullable(additionalValidators)
            .orElse(empty());
    }
}
