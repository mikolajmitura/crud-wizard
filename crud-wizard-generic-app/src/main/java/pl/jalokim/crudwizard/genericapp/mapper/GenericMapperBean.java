package pl.jalokim.crudwizard.genericapp.mapper;

import lombok.RequiredArgsConstructor;
import pl.jalokim.crudwizard.core.datastorage.RawEntity;
import pl.jalokim.crudwizard.genericapp.config.GenericMapper;
import pl.jalokim.crudwizard.genericapp.config.GenericMethod;

@RequiredArgsConstructor
@GenericMapper
public class GenericMapperBean {

    @GenericMethod
    public RawEntity mapToTarget(MapperArgument mapperArgument) {
        if (mapperArgument.getTargetMetaModel() == mapperArgument.getSourceMetaModel()) {
            return RawEntity.fromMap(mapperArgument.getSourceObject());
        }
        // TODO should map from one meta model to another meta model.
        // another is auto mapping the same fields with names and the same types or with auto conversion from one to another.
        return null;
    }
}
