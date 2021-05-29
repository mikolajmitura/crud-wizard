package pl.jalokim.crudwizard.genericapp.metamodel.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import pl.jalokim.crudwizard.genericapp.metamodel.AdditionalPropertyMetaModel;

@EqualsAndHashCode(callSuper = true)
@Value
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class DataStorageMetaModel extends AdditionalPropertyMetaModel {

    Long id;

    String name;

    String className;
}
