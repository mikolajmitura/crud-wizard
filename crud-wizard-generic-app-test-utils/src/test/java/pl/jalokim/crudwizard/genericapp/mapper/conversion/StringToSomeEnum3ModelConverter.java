package pl.jalokim.crudwizard.genericapp.mapper.conversion;

import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;

@Component
public class StringToSomeEnum3ModelConverter implements ClassMetaModelConverter<String, String> {

    @Override
    public String convert(String from) {
        if (from.equals("3")) {
            return "VAR3";
        }
        return from;
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
            .name("someEnum3Model")
            .build();
    }
}
