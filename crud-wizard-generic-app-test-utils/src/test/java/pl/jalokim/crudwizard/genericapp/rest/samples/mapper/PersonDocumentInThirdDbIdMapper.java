package pl.jalokim.crudwizard.genericapp.rest.samples.mapper;

import java.util.Optional;
import pl.jalokim.crudwizard.genericapp.mapper.GenericMapperArgument;

public class PersonDocumentInThirdDbIdMapper {

    String mapToUuid(GenericMapperArgument genericMapperArgument) {
        return (String) Optional.ofNullable(genericMapperArgument.getRequestParams())
            .map(requestParams -> requestParams.get("thirdDbId"))
            .orElse(null);
    }
}
