package pl.jalokim.crudwizard.genericapp.mapper.instance.objects;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class SomeBeanWithGenerics<I, V> {

   I id;
   V listOf;
}
