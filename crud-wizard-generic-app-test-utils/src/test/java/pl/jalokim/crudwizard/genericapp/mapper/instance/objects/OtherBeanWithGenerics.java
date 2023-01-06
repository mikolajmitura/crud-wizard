package pl.jalokim.crudwizard.genericapp.mapper.instance.objects;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OtherBeanWithGenerics<S> {

    Long id;
    List<S> listOf;
}
