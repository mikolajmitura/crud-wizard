package pl.jalokim.crudwizard.genericapp.metamodel.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import pl.jalokim.crudwizard.core.metamodels.AdditionalPropertyMetaModelDto;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class ServiceMetaModelDto extends AdditionalPropertyMetaModelDto {

    Long id;
    String className;
    String beanName;
    String methodName;
    String serviceScript;

}
