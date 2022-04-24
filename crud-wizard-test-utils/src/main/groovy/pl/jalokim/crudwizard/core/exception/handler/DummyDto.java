package pl.jalokim.crudwizard.core.exception.handler;

import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.jalokim.crudwizard.core.translations.ExampleEnum;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SomeCustomAnnotation(exampleEnum = ExampleEnum.ENUM2)
class DummyDto {

    @NotNull
    private String mandatoryField;

    @Pattern(regexp = "^[A-Za-z0-9]*$")
    private String alphanumericString;

    @SomeCustomAnnotation(exampleEnum = ExampleEnum.ENUM2)
    private Long firstLong;

    @SomeCustomAnnotation(exampleEnum = ExampleEnum.ENUM2)
    private Long secondLong;

    @SomeCustomAnnotation(exampleEnum = ExampleEnum.TEST)
    private Long thirdLong;

    private List<InnerDummyDto> someList;

    private String fieldForTestMessagePlaceholder;
}
