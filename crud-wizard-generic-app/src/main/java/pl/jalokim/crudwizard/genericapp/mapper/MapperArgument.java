package pl.jalokim.crudwizard.genericapp.mapper;

import lombok.Builder;
import lombok.Value;
import pl.jalokim.crudwizard.core.datastorage.RawEntityObject;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;

@Value
@Builder
public class MapperArgument {

    ClassMetaModel sourceMetaModel;
    RawEntityObject sourceObject;
    ClassMetaModel targetMetaModel;

}
