package pl.jalokim.crudwizard.genericapp.mapper.generete.method;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.translatePlaceholder;

import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.mapper.conversion.GenericObjectsConversionService;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MapperCodeMetadata;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperConfiguration;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperGenerateConfiguration;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.NullAssignExpression;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ValueToAssignCodeMetadata;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ValueToAssignExpression;
import pl.jalokim.crudwizard.genericapp.service.translator.ObjectNodePath;

@Component
@RequiredArgsConstructor
class AssignExpressionAsTextResolver {

    private final GenericObjectsConversionService genericObjectsConversionService;
    private final ConversionService conversionService;

    String getExpressionForAssignWhenExists(MapperMethodGeneratorArgument methodGeneratorArgument,
        AtomicReference<ValueToAssignExpression> assignExpressionForFieldReference,
        TargetFieldMetaData targetFieldMetaData,
        String mappingProblemReason) {

        String methodName = methodGeneratorArgument.getMethodName();
        boolean generated = methodGeneratorArgument.isGenerated();
        ClassMetaModel targetMetaModel = methodGeneratorArgument.getTargetMetaModel();
        MapperCodeMetadata mapperGeneratedCodeMetadata = methodGeneratorArgument.getMapperGeneratedCodeMetadata();
        MapperConfiguration mapperConfiguration = methodGeneratorArgument.getMapperConfiguration();
        MapperGenerateConfiguration mapperGenerateConfiguration = methodGeneratorArgument.getMapperGenerateConfiguration();

        String fieldName = targetFieldMetaData.getFieldName();
        ObjectNodePath fieldNameNodePath = targetFieldMetaData.getFieldNameNodePath();
        ClassMetaModel targetFieldClassMetaModel = targetFieldMetaData.getTargetFieldClassMetaModel();

        if (assignExpressionForFieldReference.get() == null) {
            if (mapperGenerateConfiguration.isGlobalIgnoreMappingProblems()
                || mapperConfiguration.isIgnoreMappingProblems()
                || targetFieldMetaData.getPropertiesOverriddenMappingForField().isIgnoreMappingProblem()) {
                assignExpressionForFieldReference.set(new NullAssignExpression(targetFieldClassMetaModel));
            } else {
                String inMethodPartMessage = generated ? "" : translatePlaceholder("mapper.not.found.assign.for.method", methodName);
                String reasonPart = mappingProblemReason == null ? "" : translatePlaceholder("mapper.mapping.problem.reason", mappingProblemReason);

                mapperGeneratedCodeMetadata.addError(createMessagePlaceholder("mapper.not.found.assign.strategy",
                    fieldName, targetMetaModel.getTypeDescription(), fieldNameNodePath.getFullPath(), inMethodPartMessage + reasonPart));
            }
        }

        if (assignExpressionForFieldReference.get() != null) {
            ValueToAssignCodeMetadata valueToAssignCodeMetadata = assignExpressionForFieldReference.get()
                .generateCodeMetadata(mapperGeneratedCodeMetadata);

            return generateFetchValueForAssign(valueToAssignCodeMetadata.getReturnClassModel(),
                targetFieldClassMetaModel, valueToAssignCodeMetadata.getFullValueExpression(),
                mapperGeneratedCodeMetadata, fieldNameNodePath);
        }
        return null;
    }

    private String generateFetchValueForAssign(ClassMetaModel sourceMetaModel, ClassMetaModel targetMetaModel,
        String fetchValueExpression, MapperCodeMetadata mapperGeneratedCodeMetadata, ObjectNodePath currentPath) {

        if (isTheSameTypeOrSubType(targetMetaModel, sourceMetaModel)) {
            return fetchValueExpression;
        } else {
            var converterDefinition = genericObjectsConversionService.findConverterDefinition(sourceMetaModel, targetMetaModel);
            if (converterDefinition != null) {
                mapperGeneratedCodeMetadata.addConstructorArgument(GenericObjectsConversionService.class);
                String converterName = converterDefinition.getBeanName();
                return String.format("((%s) genericObjectsConversionService.convert(\"%s\", %s))",
                    targetMetaModel.getJavaGenericTypeInfo(), converterName, fetchValueExpression);
            } else if (sourceMetaModel.hasRealClass() && targetMetaModel.hasRealClass()
                && conversionService.canConvert(sourceMetaModel.getRealClass(), targetMetaModel.getRealClass())) {
                mapperGeneratedCodeMetadata.addConstructorArgument(ConversionService.class);
                return String.format("conversionService.convert(%s, %s.class)",
                    fetchValueExpression, targetMetaModel.getCanonicalNameOfRealClass());
            } else if (whenConversionBetweenStringAndEnumMetaModel(sourceMetaModel, targetMetaModel)) {
                return fetchValueExpression;
            }
            else {
                mapperGeneratedCodeMetadata.throwMappingError(createMessagePlaceholder(
                    "mapper.converter.not.found.between.metamodels",
                    sourceMetaModel.getTypeDescription(),
                    targetMetaModel.getTypeDescription(),
                    currentPath.getFullPath()));
                return null;
            }
        }
    }

    private boolean whenConversionBetweenStringAndEnumMetaModel(ClassMetaModel sourceMetaModel, ClassMetaModel targetMetaModel) {
        return (targetMetaModel.isGenericMetamodelEnum() && String.class.equals(sourceMetaModel.getRealClass()))
            || (sourceMetaModel.isGenericMetamodelEnum() && String.class.equals(targetMetaModel.getRealClass()));
    }

    private boolean isTheSameTypeOrSubType(ClassMetaModel targetMetaModel, ClassMetaModel sourceMetaModel) {
        return sourceMetaModel.getTypeDescription().equals(targetMetaModel.getTypeDescription())
            || sourceMetaModel.isSubTypeOf(targetMetaModel);
    }
}
