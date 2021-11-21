package pl.jalokim.crudwizard.genericapp.metamodel.datastorage;

import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_NULL;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NULL;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import pl.jalokim.crudwizard.core.metamodels.AdditionalPropertyDto;
import pl.jalokim.crudwizard.core.metamodels.AdditionalPropertyMetaModelDto;
import pl.jalokim.crudwizard.core.validation.javax.FieldShouldWhenOther;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldShouldWhenOther(field = "name", should = NOT_NULL, whenField = "id", is = NULL)
@FieldShouldWhenOther(field = "className", should = NOT_NULL, whenField = "id", is = NULL)
public class DataStorageMetaModelDto extends AdditionalPropertyMetaModelDto {

    Long id;

    String name;

    String className;

    @Builder.Default
    List<AdditionalPropertyDto> additionalProperties = new ArrayList<>();
}
