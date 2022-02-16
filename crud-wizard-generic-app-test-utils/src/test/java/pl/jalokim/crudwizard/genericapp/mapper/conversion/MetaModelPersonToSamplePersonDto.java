package pl.jalokim.crudwizard.genericapp.mapper.conversion;

import java.util.Map;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.core.sample.SamplePersonDto;

@Component
public class MetaModelPersonToSamplePersonDto
    implements ClassMetaModelConverter<Map<String, Object>, SamplePersonDto> {

    @Override
    public SamplePersonDto convert(Map<String, Object> from) {
        return SamplePersonDto.builder()
            .id((Long) from.get("personId"))
            .name((String) from.get("personName"))
            .build();
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
            .realClass(SamplePersonDto.class)
            .build();
    }
}
