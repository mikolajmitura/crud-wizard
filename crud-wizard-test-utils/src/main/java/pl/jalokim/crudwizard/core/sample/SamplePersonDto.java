package pl.jalokim.crudwizard.core.sample;

import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.jalokim.crudwizard.core.validation.javax.groups.UpdateContext;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class SamplePersonDto {

    @NotNull(groups = UpdateContext.class)
    private Long id;
    @NotNull
    private String name;
    @NotNull
    private String surname;

    private LocalDate birthDay;

    private LocalDateTime lastLogin;

}
