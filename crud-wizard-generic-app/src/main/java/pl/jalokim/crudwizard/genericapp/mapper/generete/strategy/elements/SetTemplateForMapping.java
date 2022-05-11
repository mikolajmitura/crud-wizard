package pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.elements;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MapperCodeMetadata;

public class SetTemplateForMapping extends CollectionTemplateForMapping {

    @Override
    public String generateNewIterable(List<ClassMetaModel> genericTypesOfIterable, MapperCodeMetadata mapperCodeMetadata) {
        mapperCodeMetadata.addImport(HashSet.class);
        return "new HashSet<>(initSize)";
    }

    @Override
    public String generateIterableType(List<ClassMetaModel> genericTypesOfIterable, MapperCodeMetadata mapperCodeMetadata) {
        mapperCodeMetadata.addImport(Set.class);
        return "        Set<" + contactGenerics(genericTypesOfIterable) + ">";
    }
}
