package pl.jalokim.crudwizard.genericapp.mapper.conversion;

import java.util.Optional;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.sample.SamplePersonDto;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;

@Component("SamplePersonDto-to-string-bean")
public class FromSamplePersonDtoToStringConverter
    implements ClassMetaModelConverter<SamplePersonDto, String> {

    @Override
    public String convert(SamplePersonDto from) {
        return Optional.ofNullable(from)
            .map(value -> "person id:" + value.getId())
            .orElse(null);
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
            .realClass(String.class)
            .build();
    }
}
