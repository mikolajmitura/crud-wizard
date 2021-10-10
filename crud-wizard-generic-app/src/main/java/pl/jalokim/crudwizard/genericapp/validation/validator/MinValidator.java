package pl.jalokim.crudwizard.genericapp.validation.validator;

import static java.util.Collections.unmodifiableMap;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.validation.constraints.Min;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForBigDecimal;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForBigInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForDouble;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForFloat;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForLong;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForNumber;
import pl.jalokim.crudwizard.genericapp.validation.ValidationSessionContext;

public class MinValidator extends JavaxProxyDataValidator<Min, Number> {

    public static final String VALIDATOR_KEY_SIZE = "MIN";
    public static final String MIN_ARG_NAME = "min_value";

    private static final Map<Class<?>, Class<?>> JAVAX_SIZE_VALIDATORS_BY_CLASS = buildJavaxValidatorsByClass();

    private static Map<Class<?>, Class<?>> buildJavaxValidatorsByClass() {
        Map<Class<?>, Class<?>> javaxValidatorsByClass = new LinkedHashMap<>();
        javaxValidatorsByClass.put(BigDecimal.class, MinValidatorForBigDecimal.class);
        javaxValidatorsByClass.put(BigInteger.class, MinValidatorForBigInteger.class);
        javaxValidatorsByClass.put(Double.class, MinValidatorForDouble.class);
        javaxValidatorsByClass.put(Float.class, MinValidatorForFloat.class);
        javaxValidatorsByClass.put(Long.class, MinValidatorForLong.class);
        javaxValidatorsByClass.put(Number.class, MinValidatorForNumber.class);
        return unmodifiableMap(javaxValidatorsByClass);
    }

    @Override
    public String validatorName() {
        return VALIDATOR_KEY_SIZE;
    }

    @Override
    public Map<Class<?>, Class<?>> getValidatorsByType() {
        return JAVAX_SIZE_VALIDATORS_BY_CLASS;
    }

    @Override
    public Map<String, Object> messagePlaceholderArgs(ValidationSessionContext validationContext) {
        return Map.of(
            "value", validationContext.getValidatorArgument(MIN_ARG_NAME)
        );
    }
}
