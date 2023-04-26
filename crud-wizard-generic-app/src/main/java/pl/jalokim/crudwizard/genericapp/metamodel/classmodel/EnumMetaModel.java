package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import static pl.jalokim.utils.collection.Elements.elements;

import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode()
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder(toBuilder = true)
public class EnumMetaModel {

    public List<String> getEnumValues() {
        return elements(enums)
            .map(EnumEntryMetaModel::getName)
            .asList();
    }

    List<EnumEntryMetaModel> enums;

    public EnumEntryMetaModel getEnumByName(String enumName) {
        return elements(enums)
            .filter(enumEntry -> enumEntry.getName().equals(enumName))
            .getFirst();
    }
}
