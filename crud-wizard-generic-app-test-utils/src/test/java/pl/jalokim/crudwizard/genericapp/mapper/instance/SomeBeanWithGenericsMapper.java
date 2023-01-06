package pl.jalokim.crudwizard.genericapp.mapper.instance;

import java.util.List;
import pl.jalokim.crudwizard.genericapp.mapper.instance.objects.OtherBeanWithGenerics;
import pl.jalokim.crudwizard.genericapp.mapper.instance.objects.SomeBeanWithGenerics;

public class SomeBeanWithGenericsMapper extends SomeBeanWithGenericsMapperParent<Long, List<String>, String> {

    @Override
    public OtherBeanWithGenerics<String> map(SomeBeanWithGenerics<Long, List<String>> someBeanWithGenerics) {
        return OtherBeanWithGenerics.<String>builder()
            .id(someBeanWithGenerics.getId())
            .listOf(someBeanWithGenerics.getListOf())
            .build();
    }
}
