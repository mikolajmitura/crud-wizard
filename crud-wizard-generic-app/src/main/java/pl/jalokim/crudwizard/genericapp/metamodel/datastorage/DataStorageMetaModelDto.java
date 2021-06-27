package pl.jalokim.crudwizard.genericapp.metamodel.datastorage;

import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NOT_NULL;
import static pl.jalokim.crudwizard.core.validation.javax.ExpectedFieldState.NULL;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import pl.jalokim.crudwizard.core.metamodels.AdditionalPropertyMetaModelDto;
import pl.jalokim.crudwizard.core.validation.javax.FieldShouldWhenOther;

@EqualsAndHashCode(callSuper = true)
@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@FieldShouldWhenOther(field = "name", should = NOT_NULL, whenField = "id", is = NULL)
@FieldShouldWhenOther(field = "className", should = NOT_NULL, whenField = "id", is = NULL)
public class DataStorageMetaModelDto extends AdditionalPropertyMetaModelDto {

    Long id;

    String name;

    String className;

}
