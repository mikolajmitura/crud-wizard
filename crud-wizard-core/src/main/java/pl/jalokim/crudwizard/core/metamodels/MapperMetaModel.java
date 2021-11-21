package pl.jalokim.crudwizard.core.metamodels;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MapperMetaModel extends AdditionalPropertyMetaModelDto {

    Long id;
    Object mapperInstance;
    String className;
    String beanName;
    String methodName;
    BeanMethodMetaModel methodMetaModel;

    // TODO script will be loaded to mapperInstance in lazy way and cached then
    String mapperScript;
    // TODO it will be used anywhere???
    MappingDirection mappingDirection;

    @Builder.Default
    List<AdditionalPropertyDto> additionalProperties = new ArrayList<>();
}
