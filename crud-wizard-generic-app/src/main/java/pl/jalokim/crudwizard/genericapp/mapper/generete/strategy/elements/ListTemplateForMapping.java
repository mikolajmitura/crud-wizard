package pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.elements;

import java.util.ArrayList;
import java.util.List;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MapperCodeMetadata;

public class ListTemplateForMapping extends CollectionTemplateForMapping {

    @Override
    public String generateNewIterable(List<ClassMetaModel> genericTypesOfIterable, MapperCodeMetadata mapperCodeMetadata) {
        mapperCodeMetadata.addImport(ArrayList.class);
        return "new ArrayList<>(initSize)";
    }

    @Override
    public String generateIterableType(List<ClassMetaModel> genericTypesOfIterable, MapperCodeMetadata mapperCodeMetadata) {
        mapperCodeMetadata.addImport(List.class);
        return "        List<" + contactGenerics(genericTypesOfIterable) +">";
    }
}
