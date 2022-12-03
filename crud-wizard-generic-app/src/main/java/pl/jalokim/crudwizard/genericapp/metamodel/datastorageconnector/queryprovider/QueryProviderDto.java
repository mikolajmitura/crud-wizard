package pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.queryprovider;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import pl.jalokim.crudwizard.core.validation.javax.ClassExists;
import pl.jalokim.crudwizard.genericapp.datastorage.query.DataStorageQueryProvider;
import pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty.WithAdditionalPropertiesDto;

@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@Jacksonized
@SuperBuilder(toBuilder = true)
public class QueryProviderDto extends WithAdditionalPropertiesDto {

    Long id;

    @NotNull
    @ClassExists(expectedOfType = DataStorageQueryProvider.class)
    @Size(min = 3, max = 250)
    String className;

    String rawQueryCode;

}
