package pl.jalokim.crudwizard.core.exception.handler;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;

import javax.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import pl.jalokim.crudwizard.core.translations.AppMessageSource;
import pl.jalokim.crudwizard.core.translations.ExampleEnum;
import pl.jalokim.crudwizard.core.translations.MessagePlaceholder;
import pl.jalokim.crudwizard.core.validation.javax.base.BaseConstraintValidator;
import pl.jalokim.crudwizard.core.validation.javax.base.PropertyPath;

@RequiredArgsConstructor
public class SomeCustomAnnotationValidator implements BaseConstraintValidator<SomeCustomAnnotation, Object> {

    private final AppMessageSource appMessageSource;
    private ExampleEnum exampleEnum;

    @Override
    public void initialize(SomeCustomAnnotation constraintAnnotation) {
        exampleEnum = constraintAnnotation.exampleEnum();
    }

    @Override
    @SuppressWarnings({
        "PMD.CognitiveComplexity",
        "PMD.NPathComplexity",
        "PMD.AvoidCatchingNPE",
        "PMD.EmptyCatchBlock"
    })
    public boolean isValidValue(Object object, ConstraintValidatorContext context) {
        if (object instanceof Long) {
            long value = (Long) object;
            if (value > 10) {
                return true;
            }
            if (value == 1) {
                addMessageParameter(context, "exampleEnum", appMessageSource.getMessageByEnum(exampleEnum));
                return false;
            }
            if (value == 2) {
                customMessage(context, "some custom validation message");
                return false;
            }
            return false;
        }
        if (object instanceof DummyDto) {
            DummyDto dummyDto = (DummyDto) object;
            boolean valid = true;

            if (dummyDto.getFieldForTestMessagePlaceholder() != null && dummyDto.getFieldForTestMessagePlaceholder().equals("invalid")) {
                customMessage(context, createMessagePlaceholder(getClass(), "test.placeholder.validation",
                    MessagePlaceholder.wrapAsExternalPlaceholder("some.field1"),
                    MessagePlaceholder.wrapAsExternalPlaceholder("some.field2")));

                customMessage(context, createMessagePlaceholder(getClass(), "test.placeholder.validation",
                    MessagePlaceholder.wrapAsExternalPlaceholder("some.field1"),
                    MessagePlaceholder.wrapAsExternalPlaceholder("some.field2")),
                    PropertyPath.builder()
                        .addNextProperty("thirdLong")
                        .build());
                valid = false;
            }

            for (int i = 0; i < dummyDto.getSomeList().size(); i++) {
                InnerDummyDto innerDummyDto = dummyDto.getSomeList().get(i);
                if (innerDummyDto.getSomeLong() > 5) {
                    customMessage(context, "higher than 5 index: " + i,
                        PropertyPath.builder()
                            .addNextPropertyAndIndex("someList", i)
                            .addNextProperty("someLong")
                            .build()

                    );
                    valid = false;
                }
            }

            try {
                if (dummyDto.getThirdLong() != null) {
                    customMessage(context, "thirdLong message", "thirdLong");
                    valid = false;
                }
                if (dummyDto.getSomeList().get(0).getInnerDummyDto1()
                    .getInnerDummyDto2().getAnotherList() != null) {
                    valid = false;
                    customMessage(context, "long property message",
                        PropertyPath.builder()
                            .addNextPropertyAndIndex("someList", 0)
                            .addNextProperty("innerDummyDto1")
                            .addNextProperty("innerDummyDto2")
                            .addNextProperty("anotherList")
                            .build());
                }
            } catch (NullPointerException nullPointerException) {
                // skip
            }

            return valid;
        }
        return true;
    }
}
