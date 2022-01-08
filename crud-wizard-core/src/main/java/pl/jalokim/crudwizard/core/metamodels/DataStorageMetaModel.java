package pl.jalokim.crudwizard.core.metamodels;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import pl.jalokim.crudwizard.core.datastorage.DataStorage;

@Data
@NoArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DataStorageMetaModel extends AdditionalPropertyMetaModelDto {

    Long id;

    String name;

    DataStorage dataStorage;

    @Builder.Default
    List<AdditionalPropertyDto> additionalProperties = new ArrayList<>();
}
