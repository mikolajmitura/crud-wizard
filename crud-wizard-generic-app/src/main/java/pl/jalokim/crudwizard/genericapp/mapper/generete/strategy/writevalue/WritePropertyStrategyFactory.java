package pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.writevalue;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;
import static pl.jalokim.crudwizard.core.utils.ReflectionUtils.hasBuilderMethod;
import static pl.jalokim.crudwizard.core.utils.ReflectionUtils.hasOneConstructorMaxArgNumbers;
import static pl.jalokim.crudwizard.core.utils.ReflectionUtils.hasOnlyDefaultConstructor;

import lombok.experimental.UtilityClass;
import pl.jalokim.crudwizard.core.exception.TechnicalException;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;

@UtilityClass
public class WritePropertyStrategyFactory {

    public static WritePropertyStrategy createWritePropertyStrategy(ClassMetaModel classMetaModel) {
        if (classMetaModel.isOnlyRawClassModel()) {
            Class<?> someClass = classMetaModel.getRealClass();
            if (hasBuilderMethod(someClass)) {
                return new WriteByBuilderStrategy();
            } else if (hasOnlyDefaultConstructor(someClass)) {
                return new WriteBySettersStrategy();
            } else {
                if (hasOneConstructorMaxArgNumbers(someClass)) {
                    return new WriteByAllConstructorArgsStrategy();
                }
                throw new TechnicalException(createMessagePlaceholder(
                    "cannot.find.write.property.strategy", someClass.getCanonicalName()));
            }
        }
        return new WriteToMapStrategy();
    }
}
