package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import pl.jalokim.crudwizard.core.metamodels.AdditionalPropertyMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.endpoint.FieldMetaModelDto;
import pl.jalokim.crudwizard.genericapp.metamodel.validator.ValidatorMetaModelDto;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class ClassMetaModelDto extends AdditionalPropertyMetaModelDto {

    Long id;

    String name;

    String className;

    List<ClassMetaModelDto> genericTypes;
    List<FieldMetaModelDto> fields;
    List<ValidatorMetaModelDto> validators;

    List<ClassMetaModelDto> extendsFromModels;

}
