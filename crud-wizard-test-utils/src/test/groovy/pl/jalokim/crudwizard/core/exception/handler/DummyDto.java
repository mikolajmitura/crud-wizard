package pl.jalokim.crudwizard.core.exception.handler;

import static pl.jalokim.crudwizard.core.translations.ExampleEnum.ENUM2;
import static pl.jalokim.crudwizard.core.translations.ExampleEnum.TEST;

import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SomeCustomAnnotation(exampleEnum = ENUM2)
class DummyDto {

    @NotNull
    private String mandatoryField;

    @Pattern(regexp = "^[A-Za-z0-9]*$")
    private String alphanumericString;

    @SomeCustomAnnotation(exampleEnum = ENUM2)
    private Long firstLong;

    @SomeCustomAnnotation(exampleEnum = ENUM2)
    private Long secondLong;

    @SomeCustomAnnotation(exampleEnum = TEST)
    private Long thirdLong;

    private List<InnerDummyDto> someList;

    private String fieldForTestMessagePlaceholder;
}
