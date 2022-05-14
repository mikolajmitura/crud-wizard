package pl.jalokim.crudwizard.genericapp.mapper.generete.method;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.genericapp.mapper.generete.MapperArgumentMethodModel;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MapperCodeMetadata;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MethodCodeMetadata.MethodCodeMetadataBuilder;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.RawJavaCodeAssignExpression;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ValueToAssignExpression;
import pl.jalokim.crudwizard.genericapp.service.translator.ObjectNodePath;

@Component
@RequiredArgsConstructor
class SimpleTargetAssignResolver {

    private final AssignExpressionAsTextResolver assignExpressionAsTextResolver;

    void generateMapperMethodWhenMapToSimpleType(List<MapperArgumentMethodModel> methodArguments,
        MapperMethodGeneratorArgument methodGeneratorArgument,
        MethodCodeMetadataBuilder methodBuilder, TargetFieldMetaData returnMethodMetaData) {

        var currentNodeOverriddenMappings = methodGeneratorArgument.findOverriddenMappingStrategiesForCurrentNode();
        AtomicReference<ValueToAssignExpression> assignExpressionForFieldReference = new AtomicReference<>();

        if (currentNodeOverriddenMappings.isEmpty()) {
            currentNodeOverriddenMappings = methodArguments.stream()
                .map(argument -> new RawJavaCodeAssignExpression(argument.getArgumentType(), "sourceObject"))
                .collect(Collectors.toList());
        }

        assignValueForSimpleField(methodGeneratorArgument,
            assignExpressionForFieldReference, returnMethodMetaData,
            currentNodeOverriddenMappings);

        String expression = assignExpressionAsTextResolver.getExpressionForAssignWhenExists(methodGeneratorArgument,
            assignExpressionForFieldReference,
            returnMethodMetaData,
            null);

        if (expression != null) {
            methodBuilder.lastLine("return " + expression);
        }
    }

    void assignValueForSimpleField(MapperMethodGeneratorArgument methodGeneratorArgument,
        AtomicReference<ValueToAssignExpression> assignExpressionForFieldReference,
        TargetFieldMetaData targetFieldMetaData,
        List<ValueToAssignExpression> foundAssignExpressionsForField) {

        String fieldName = targetFieldMetaData.getFieldName();
        MapperCodeMetadata mapperGeneratedCodeMetadata = methodGeneratorArgument.getMapperGeneratedCodeMetadata();
        ObjectNodePath currentPath = methodGeneratorArgument.getCurrentPath();

        if (assignExpressionForFieldReference.get() == null) {
            if (foundAssignExpressionsForField.size() > 1) {
                mapperGeneratedCodeMetadata.throwMappingError(
                    createMessagePlaceholder("mapper.found.to.many.mappings.for.simple.type",
                        currentPath.nextNode(fieldName).getFullPath())
                );
            } else if (foundAssignExpressionsForField.size() == 1) {
                assignExpressionForFieldReference.set(foundAssignExpressionsForField.get(0));
            }
        }
    }

}
