package pl.jalokim.crudwizard.genericapp.validation.validator

import pl.jalokim.crudwizard.genericapp.validation.ValidationSessionContext
import spock.lang.Specification
import spock.lang.Unroll

@SuppressWarnings("EmptyClass")
class DataValidatorTest extends Specification {

    @Unroll
    def "return expected class for certain validator"() {
        when:
        def resultClass = validator.typesToValidate

        then:
        resultClass as Set == expectedTypes as Set

        where:
        validator                      | expectedTypes
        new NotNullValidator()         | [Object.class]
        new SizeValidator()            | [Collection.class,
                                          boolean[].class,
                                          byte[].class,
                                          char[].class,
                                          double[].class,
                                          float[].class,
                                          int[].class,
                                          long[].class,
                                          short[].class,
                                          Map.class,
                                          Object[].class,
                                          CharSequence.class]
        new ExampleValidator2()        | [DataValidatorTest.class]
        new WithoutDataValidatorType() | [Object.class]
    }

    private static class ExampleValidator2 extends ExampleValidator1 {

    }

    private static class ExampleValidator1 extends ExampleClass implements ExampleInterface, DataValidator<DataValidatorTest> {

        @Override
        boolean isValid(DataValidatorTest value, ValidationSessionContext validationContext) {
            return false
        }

        @Override
        String validatorName() {
            return null
        }
    }

    static class ExampleClass {

    }

    static interface ExampleInterface {

    }

    static class WithoutDataValidatorType implements DataValidator {

        @Override
        boolean isValid(Object value, ValidationSessionContext validationContext) {
            return false
        }

        @Override
        String validatorName() {
            return null
        }
    }
}
