package pl.jalokim.crudwizard.genericapp.rest.samples.dto;

import lombok.Value;
import pl.jalokim.crudwizard.core.validation.javax.IdExists;
import pl.jalokim.crudwizard.genericapp.rest.samples.entity.UniqueValueSampleWithCustomTableEntity;
import pl.jalokim.crudwizard.genericapp.rest.samples.entity.WithLongIdEntity;

@Value
public class SomeExistIdDto {

    @IdExists(entityClass = WithLongIdEntity.class)
    Long someId;

    @IdExists(entityClass = UniqueValueSampleWithCustomTableEntity.class)
    String someUuid;
}
