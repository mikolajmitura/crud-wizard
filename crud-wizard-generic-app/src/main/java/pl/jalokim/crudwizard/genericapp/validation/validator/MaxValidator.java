package pl.jalokim.crudwizard.genericapp.validation.validator;

import static java.util.Collections.unmodifiableMap;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.validation.constraints.Max;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MaxValidatorForBigDecimal;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MaxValidatorForBigInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MaxValidatorForDouble;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MaxValidatorForFloat;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MaxValidatorForLong;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MaxValidatorForNumber;
import pl.jalokim.crudwizard.genericapp.validation.ValidationSessionContext;

public class MaxValidator extends JavaxProxyDataValidator<Max, Number> {

    public static final String VALIDATOR_KEY_SIZE = "MAX";
    public static final String MAX_ARG_NAME = "max_value";

    private static final Map<Class<?>, Class<?>> JAVAX_SIZE_VALIDATORS_BY_CLASS = buildJavaxValidatorsByClass();

    private static Map<Class<?>, Class<?>> buildJavaxValidatorsByClass() {
        Map<Class<?>, Class<?>> javaxValidatorsByClass = new LinkedHashMap<>();
        javaxValidatorsByClass.put(BigDecimal.class, MaxValidatorForBigDecimal.class);
        javaxValidatorsByClass.put(BigInteger.class, MaxValidatorForBigInteger.class);
        javaxValidatorsByClass.put(Double.class, MaxValidatorForDouble.class);
        javaxValidatorsByClass.put(Float.class, MaxValidatorForFloat.class);
        javaxValidatorsByClass.put(Long.class, MaxValidatorForLong.class);
        javaxValidatorsByClass.put(Number.class, MaxValidatorForNumber.class);
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
            "value", validationContext.getValidatorArgument(MAX_ARG_NAME)
        );
    }
}
