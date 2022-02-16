package pl.jalokim.crudwizard.genericapp.mapper.conversion;

import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;

public interface ClassMetaModelConverter<F, T> {

    T convert(F from);

    ClassMetaModel sourceMetaModel();

    ClassMetaModel targetMetaModel();
}
