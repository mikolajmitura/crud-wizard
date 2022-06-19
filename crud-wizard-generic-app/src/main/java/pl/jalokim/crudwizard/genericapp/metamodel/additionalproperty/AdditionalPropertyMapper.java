package pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty;

import org.springframework.beans.factory.annotation.Autowired;
import pl.jalokim.crudwizard.genericapp.metamodel.BaseMapper;

public abstract class AdditionalPropertyMapper<D, E, M> implements BaseMapper<D, E, M> {

    @Autowired
    private RawAdditionalPropertyMapper rawAdditionalPropertyMapper;

    public AdditionalPropertyDto additionalPropertyToDto(AdditionalPropertyEntity additionalPropertyEntity) {
        return rawAdditionalPropertyMapper.additionalPropertyToDto(additionalPropertyEntity);
    }

    public AdditionalPropertyEntity additionalPropertyToEntity(AdditionalPropertyDto additionalPropertyDto) {
        return rawAdditionalPropertyMapper.additionalPropertyToEntity(additionalPropertyDto);
    }

    public AdditionalPropertyMetaModel additionalPropertyToModel(AdditionalPropertyEntity additionalPropertyEntity) {
        return rawAdditionalPropertyMapper.additionalPropertyToModel(additionalPropertyEntity);
    }

}
