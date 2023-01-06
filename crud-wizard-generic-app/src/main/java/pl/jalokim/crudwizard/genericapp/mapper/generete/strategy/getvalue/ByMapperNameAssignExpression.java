package pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue;

import static pl.jalokim.crudwizard.core.utils.StringCaseUtils.firstLetterToLowerCase;
import static pl.jalokim.utils.collection.Elements.elements;

import java.util.List;
import lombok.Value;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MapperCodeMetadata;
import pl.jalokim.crudwizard.genericapp.mapper.invoker.MapperByNameInvoker;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextService;

@Value
public class ByMapperNameAssignExpression implements ValueToAssignExpression {

    ClassMetaModel mapperReturnClassMetaModel;
    ValueToAssignExpression valueExpression;
    String mapperName;

    @Override
    public ValueToAssignCodeMetadata generateCodeMetadata(MapperCodeMetadata mapperGeneratedCodeMetadata) {

        mapperGeneratedCodeMetadata.addConstructorArgument(MapperByNameInvoker.class);
        mapperGeneratedCodeMetadata.addConstructorArgument(MetaModelContextService.class);
        mapperGeneratedCodeMetadata.addImport(ClassMetaModel.class);
        mapperGeneratedCodeMetadata.addImport(List.class);

        ValueToAssignCodeMetadata returnCodeMetadata = new ValueToAssignCodeMetadata();
        returnCodeMetadata.setReturnClassModel(mapperReturnClassMetaModel);
        ValueToAssignCodeMetadata valueToAssignCodeMetadata = valueExpression.generateCodeMetadata(mapperGeneratedCodeMetadata);
        returnCodeMetadata.setValueGettingCode(String.format("%s.%s(\"%s\", %s, %s, %s, %s)", firstLetterToLowerCase(MapperByNameInvoker.class.getSimpleName()),
            "mapWithMapper", mapperName, "genericMapperArgument", valueToAssignCodeMetadata.getFullValueExpression(),
            generateFetchClassMetaModel(valueToAssignCodeMetadata.getReturnClassModel()), generateFetchClassMetaModel(mapperReturnClassMetaModel)));

        return returnCodeMetadata;
    }

    private String generateFetchClassMetaModel(ClassMetaModel classMetaModel) {
        if (classMetaModel.isGenericModel()) {
            return String.format(firstLetterToLowerCase(MetaModelContextService.class.getSimpleName()) +
                ".getClassMetaModelByName(\"%s\")", classMetaModel.getName());
        }
        return ClassMetaModelBuildExpression.builder()
            .realClass(classMetaModel.getRealOrBasedClass())
            .genericTypeExpressions(elements(classMetaModel.getGenericTypes())
                .map(this::generateFetchClassMetaModel)
                .asList()).build()
            .toString();
    }
}
