package pl.jalokim.crudwizard.genericapp.mapper.conversion;

import java.util.Map;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.sample.SamplePersonDto;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;

@Component
public class SamplePersonDtoToMetaModelPerson
    implements ClassMetaModelConverter<SamplePersonDto, Map<String, Object>> {

    @Override
    public Map<String, Object> convert(SamplePersonDto from) {
        return Map.of(
            "personId", from.getId(),
            "personName", from.getName()
        );
    }

    @Override
    public ClassMetaModel sourceMetaModel() {
        return ClassMetaModel.builder()
            .realClass(SamplePersonDto.class)
            .build();
    }

    @Override
    public ClassMetaModel targetMetaModel() {
        return ClassMetaModel.builder()
            .name("person")
            .build();
    }
}
