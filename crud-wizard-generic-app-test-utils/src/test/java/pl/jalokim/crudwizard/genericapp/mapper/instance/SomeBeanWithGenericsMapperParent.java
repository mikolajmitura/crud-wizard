package pl.jalokim.crudwizard.genericapp.mapper.instance;

import pl.jalokim.crudwizard.genericapp.mapper.instance.objects.OtherBeanWithGenerics;
import pl.jalokim.crudwizard.genericapp.mapper.instance.objects.SomeBeanWithGenerics;

public abstract class SomeBeanWithGenericsMapperParent<I, V, E> {

    public abstract OtherBeanWithGenerics<E> map(SomeBeanWithGenerics<I, V> someBeanWithGenerics);
}
