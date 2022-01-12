package pl.jalokim.crudwizard.genericapp.metamodel.datastorage;

import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_NULL;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NULL;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import pl.jalokim.crudwizard.core.datastorage.DataStorage;
import pl.jalokim.crudwizard.core.validation.javax.ClassExists;
import pl.jalokim.crudwizard.core.validation.javax.FieldShouldWhenOther;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.WithAdditionalPropertiesDto;

@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Jacksonized
@SuperBuilder(toBuilder = true)
@FieldShouldWhenOther(field = "name", should = NOT_NULL, whenField = "id", is = NULL)
@FieldShouldWhenOther(field = "className", should = NOT_NULL, whenField = "id", is = NULL)
public class DataStorageMetaModelDto extends WithAdditionalPropertiesDto {

    Long id;

    // TODO #44 verify that name of ds exists
    String name;

    @ClassExists(expectedOfType = DataStorage.class)
    String className;
}
