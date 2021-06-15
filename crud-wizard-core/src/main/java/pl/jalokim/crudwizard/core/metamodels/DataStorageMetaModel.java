package pl.jalokim.crudwizard.core.metamodels;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import pl.jalokim.crudwizard.core.metamodels.AdditionalPropertyMetaModel;

@EqualsAndHashCode(callSuper = true)
@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class DataStorageMetaModel extends AdditionalPropertyMetaModel {

    Long id;

    String name;

    String className;
}
