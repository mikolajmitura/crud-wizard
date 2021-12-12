package pl.jalokim.crudwizard.genericapp.metamodel.datastorageconnector.queryprovider;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import pl.jalokim.crudwizard.core.datastorage.query.DataStorageQueryProvider;
import pl.jalokim.crudwizard.core.metamodels.AdditionalPropertyDto;
import pl.jalokim.crudwizard.core.metamodels.AdditionalPropertyMetaModelDto;
import pl.jalokim.crudwizard.core.validation.javax.ClassExists;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class QueryProviderDto extends AdditionalPropertyMetaModelDto {

    Long id;

    @NotNull
    @ClassExists(expectedOfType = DataStorageQueryProvider.class)
    String className;

    String rawQueryCode;

    @Builder.Default
    List<AdditionalPropertyDto> additionalProperties = new ArrayList<>();
}
