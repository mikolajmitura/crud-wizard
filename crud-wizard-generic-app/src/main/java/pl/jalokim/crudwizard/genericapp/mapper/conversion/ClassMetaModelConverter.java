package pl.jalokim.crudwizard.genericapp.mapper.conversion;

import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;

public interface ClassMetaModelConverter<F, T> {

    T convert(F from);

    ClassMetaModel sourceMetaModel();

    ClassMetaModel targetMetaModel();
}
