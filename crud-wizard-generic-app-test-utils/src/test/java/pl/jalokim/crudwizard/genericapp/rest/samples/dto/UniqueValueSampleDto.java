package pl.jalokim.crudwizard.genericapp.rest.samples.dto;

import lombok.Builder;
import lombok.Data;
import pl.jalokim.crudwizard.core.validation.javax.UniqueValue;
import pl.jalokim.crudwizard.genericapp.rest.samples.entity.SomeUniqueValueEntity;
import pl.jalokim.crudwizard.genericapp.rest.samples.entity.UniqueValueSampleWithCustomTableEntity;

@Data
@Builder
public class UniqueValueSampleDto {

    @UniqueValue(entityClass = UniqueValueSampleWithCustomTableEntity.class)
    private String someFieldName;

    @UniqueValue(entityClass = UniqueValueSampleWithCustomTableEntity.class)
    private String fieldWithCustomColumn;

    @UniqueValue(entityClass = SomeUniqueValueEntity.class,
        entityFieldName = "fieldWithCustomColumn2")
    private String fieldWithCustomEntityFieldName;
}
