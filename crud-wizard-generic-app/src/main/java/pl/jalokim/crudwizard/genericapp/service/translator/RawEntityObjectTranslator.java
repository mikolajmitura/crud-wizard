package pl.jalokim.crudwizard.genericapp.service.translator;

import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.datastorage.RawEntityObject;

@Component
public class RawEntityObjectTranslator {

    // TODO #01 translate fields, translate raw map<String, String> request to map with real classes. And Test it
    // TODO #01_1 translation failed (cannot find field name in class metamodel)
    // TODO #01_2 invalid field conversion
    // TODO #01_3 usage of other field converters translate some map node to real object bean for example to AdressDto

    public RawEntityObject translateToRealObjects(RawEntityObject sourceRawEntityObject) {
        return null;
    }
}
