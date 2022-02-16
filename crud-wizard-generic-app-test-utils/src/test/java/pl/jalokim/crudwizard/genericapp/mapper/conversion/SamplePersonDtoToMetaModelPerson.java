package pl.jalokim.crudwizard.genericapp.mapper.conversion;

import java.util.Map;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.core.sample.SamplePersonDto;

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
