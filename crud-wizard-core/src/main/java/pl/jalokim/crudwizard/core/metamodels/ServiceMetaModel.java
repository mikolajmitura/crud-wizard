package pl.jalokim.crudwizard.core.metamodels;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@EqualsAndHashCode(callSuper = true)
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class ServiceMetaModel extends AdditionalPropertyMetaModelDto {

    Long id;

    /**
     * real instance of service
     */
    Object serviceInstance;
    String className;
    String beanName;
    String methodName;
    BeanMethodMetaModel methodMetaModel;

    // TODO script will be loaded to serviceInstance in lazy way and cached then
    String serviceScript;

}
