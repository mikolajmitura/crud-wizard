package pl.jalokim.crudwizard.genericapp.service.invoker.sample;

import java.util.List;
import java.util.Map;
import lombok.Data;
import org.springframework.stereotype.Service;
import pl.jalokim.crudwizard.genericapp.service.invoker.sample.MapGenericService.SomeGenericValue;

@Service
public class MapGenericService extends ParentGenericService<Map<Long, List<String>>, SomeGenericValue<Long, Double>> {

    @Data
    public static class SomeGenericValue<T, B> {
        T tType;
        B bType;
    }
}
