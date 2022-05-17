package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.validation;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;
import static pl.jalokim.utils.collection.Elements.elements;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.exception.TechnicalException;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextService;

@Component
@RequiredArgsConstructor
public class EnumClassMetaModelValidator {

    private final MetaModelContextService metaModelContextService;

    public String getEnumValueWhenIsValid(String enumMetaModelName, String enumValueToCheck, String inPath) {
        if (enumValueToCheck == null) {
            return null;
        }
        ClassMetaModel classMetaModel = metaModelContextService.getClassMetaModelByName(enumMetaModelName);
        List<String> enumValues = classMetaModel.getEnumClassMetaModel().getEnumValues();
        if (enumValues.contains(enumValueToCheck)) {
            return enumValueToCheck;
        } else {
            throw new TechnicalException(createMessagePlaceholder("mapping.invalid.enum.value",
                enumValueToCheck, inPath, elements(enumValues).asConcatText(", "), classMetaModel.getTypeDescription()
            ));
        }
    }
}
