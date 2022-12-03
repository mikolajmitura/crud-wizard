package pl.jalokim.crudwizard.genericapp.mapper.conversion;

import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;

@Component
public class StringToSomeEnum2Converter implements ClassMetaModelConverter<String, SomeEnum2> {

    @Override
    public SomeEnum2 convert(String from) {
        return SomeEnum2.UNKNOWN;
    }

    @Override
    public ClassMetaModel sourceMetaModel() {
        return ClassMetaModel.builder()
            .realClass(String.class)
            .build();
    }

    @Override
    public ClassMetaModel targetMetaModel() {
        return  ClassMetaModel.builder()
            .realClass(SomeEnum2.class)
            .build();
    }
}
