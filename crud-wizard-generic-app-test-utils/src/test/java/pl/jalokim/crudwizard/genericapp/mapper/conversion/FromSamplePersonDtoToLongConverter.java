package pl.jalokim.crudwizard.genericapp.mapper.conversion;

import java.util.Optional;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.core.sample.SamplePersonDto;

@Component("SamplePersonDto-to-long-bean")
public class FromSamplePersonDtoToLongConverter
    implements ClassMetaModelConverter<SamplePersonDto, Long> {

    @Override
    public Long convert(SamplePersonDto from) {
        return Optional.ofNullable(from)
            .map(SamplePersonDto::getId)
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
            .realClass(Long.class)
            .build();
    }
}
