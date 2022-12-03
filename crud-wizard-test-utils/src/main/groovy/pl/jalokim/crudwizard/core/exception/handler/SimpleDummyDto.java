package pl.jalokim.crudwizard.core.exception.handler;

import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import pl.jalokim.crudwizard.core.validation.javax.groups.UpdateContext;
import pl.jalokim.utils.test.DataFakerHelper;

@Value
@Builder
public class SimpleDummyDto {

    @NotNull(groups = UpdateContext.class)
    Long id;
    @NotNull
    String someText;

    public static SimpleDummyDto validCreateSimpleDummyDto() {
        return SimpleDummyDto.builder()
            .someText(DataFakerHelper.randomText())
            .build();
    }

    public static SimpleDummyDto validUpdateSimpleDummyDto() {
        return SimpleDummyDto.builder()
            .id(DataFakerHelper.randomLong())
            .someText(DataFakerHelper.randomText())
            .build();
    }

    public static SimpleDummyDto emptySimpleDummyDto() {
        return SimpleDummyDto.builder().build();
    }
}
