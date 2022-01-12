package pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty;

import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import pl.jalokim.crudwizard.core.validation.javax.ClassExists;

@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
public class AdditionalPropertyDto {

    Long id;

    @NotNull
    String name;

    @ClassExists
    String valueRealClassName;

    String rawJson;
}
