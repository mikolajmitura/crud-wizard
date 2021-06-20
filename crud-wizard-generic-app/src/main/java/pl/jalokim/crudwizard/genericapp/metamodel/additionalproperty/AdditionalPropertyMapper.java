package pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import pl.jalokim.crudwizard.core.metamodels.AdditionalPropertyDto;
import pl.jalokim.crudwizard.genericapp.metamodel.BaseMapper;

public abstract class AdditionalPropertyMapper<D, E, M> implements BaseMapper<D, E, M> {

    @Autowired
    private RawAdditionalPropertyMapper rawAdditionalPropertyMapper;

    @SneakyThrows
    public AdditionalPropertyDto additionalPropertyToDto(AdditionalPropertyEntity additionalPropertyEntity) {
        return rawAdditionalPropertyMapper.additionalPropertyToDto(additionalPropertyEntity);
    }

    @SneakyThrows
    public AdditionalPropertyEntity additionalPropertyToEntity(AdditionalPropertyDto additionalPropertyDto) {
        return rawAdditionalPropertyMapper.additionalPropertyToEntity(additionalPropertyDto);
    }

}
