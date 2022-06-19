package pl.jalokim.crudwizard.genericapp.mapper.conversion;

import java.util.Map;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;

@Component
public class MetaModelPersonToOtherPerson
    implements ClassMetaModelConverter<Map<String, Object>, Map<String, Object>> {

    @Override
    public Map<String, Object> convert(Map<String, Object> from) {
        return Map.of(
            "persId", from.get("personId"),
            "persName", from.get("personName")
        );
    }

    @Override
    public ClassMetaModel sourceMetaModel() {
        return ClassMetaModel.builder()
            .name("person")
            .build();
    }

    @Override
    public ClassMetaModel targetMetaModel() {
        return ClassMetaModel.builder()
            .name("other-person")
            .build();
    }
}
