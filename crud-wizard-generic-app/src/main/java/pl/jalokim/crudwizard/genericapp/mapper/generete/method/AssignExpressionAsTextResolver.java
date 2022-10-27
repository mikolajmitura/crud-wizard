package pl.jalokim.crudwizard.genericapp.mapper.generete.method;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;
import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.translatePlaceholder;

import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.genericapp.mapper.conversion.GenericObjectsConversionService;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MapperCodeMetadata;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperConfiguration;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperGenerateConfiguration;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.NullAssignExpression;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ValueToAssignCodeMetadata;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ValueToAssignExpression;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.service.translator.ObjectNodePath;

@Component
@RequiredArgsConstructor
public class AssignExpressionAsTextResolver {

    private final GenericObjectsConversionService genericObjectsConversionService;
    private final ConversionService conversionService;

    String getExpressionForAssignWhenExists(MapperMethodGeneratorArgument methodGeneratorArgument,
        AtomicReference<ValueToAssignExpression> assignExpressionForFieldReference,
        TargetFieldMetaData targetFieldMetaData,
        String mappingProblemReason) {

        ClassMetaModel targetMetaModel = methodGeneratorArgument.getTargetMetaModel();
        MapperCodeMetadata mapperGeneratedCodeMetadata = methodGeneratorArgument.getMapperGeneratedCodeMetadata();
        MapperConfiguration mapperConfiguration = methodGeneratorArgument.getMapperConfiguration();
        MapperGenerateConfiguration mapperGenerateConfiguration = methodGeneratorArgument.getMapperGenerateConfiguration();

        String fieldName = targetFieldMetaData.getFieldName();
        ObjectNodePath fieldNameNodePath = targetFieldMetaData.getFieldNameNodePath();
        ClassMetaModel targetFieldClassMetaModel = targetFieldMetaData.getTargetFieldClassMetaModel();

        if (assignExpressionForFieldReference.get() == null) {
            if (canIgnore(targetFieldMetaData, mapperConfiguration, mapperGenerateConfiguration)) {
                assignExpressionForFieldReference.set(new NullAssignExpression(targetFieldClassMetaModel));
            } else {
                String inMethodPartMessage = getInMethodPartMessage(methodGeneratorArgument);
                String reasonPart = mappingProblemReason == null ? "" : " " + translatePlaceholder("mapper.mapping.problem.reason", mappingProblemReason);

                mapperGeneratedCodeMetadata.addError(createMessagePlaceholder("mapper.not.found.assign.strategy",
                    fieldName, targetMetaModel.getTypeDescription(), fieldNameNodePath.getFullPath(), inMethodPartMessage + reasonPart));
            }
        }

        if (assignExpressionForFieldReference.get() != null) {
            ValueToAssignCodeMetadata valueToAssignCodeMetadata = assignExpressionForFieldReference.get()
                .generateCodeMetadata(mapperGeneratedCodeMetadata);

            return generateFetchValueForAssign(methodGeneratorArgument,
                valueToAssignCodeMetadata.getReturnClassModel(),
                valueToAssignCodeMetadata.getFullValueExpression(),
                targetFieldMetaData);
        }
        return null;
    }

    private boolean canIgnore(TargetFieldMetaData targetFieldMetaData, MapperConfiguration mapperConfiguration,
        MapperGenerateConfiguration mapperGenerateConfiguration) {
        return mapperGenerateConfiguration.isGlobalIgnoreMappingProblems()
            || mapperConfiguration.isIgnoreMappingProblems()
            || targetFieldMetaData.getPropertiesOverriddenMappingForField().isIgnoreMappingProblem();
    }

    public static String getInMethodPartMessage(MapperMethodGeneratorArgument methodGeneratorArgument) {
        MapperMethodGeneratorArgument currentMethodArgument = methodGeneratorArgument;
        while (currentMethodArgument != null) {
            if (!currentMethodArgument.isGenerated()) {
                return occurredInNotGeneratedMethod(currentMethodArgument.getMethodName());
            }
            currentMethodArgument = currentMethodArgument.getParentMapperMethodGeneratorArgument();
        }
        return "";
    }

    public static String occurredInNotGeneratedMethod(String methodName) {
        return " " + createMessagePlaceholder("mapper.not.found.assign.for.method", methodName);
    }

    private String generateFetchValueForAssign(MapperMethodGeneratorArgument methodGeneratorArgument,
        ClassMetaModel sourceMetaModel, String fetchValueExpression, TargetFieldMetaData targetFieldMetaData) {

        MapperCodeMetadata mapperGeneratedCodeMetadata = methodGeneratorArgument.getMapperGeneratedCodeMetadata();
        MapperConfiguration mapperConfiguration = methodGeneratorArgument.getMapperConfiguration();
        MapperGenerateConfiguration mapperGenerateConfiguration = methodGeneratorArgument.getMapperGenerateConfiguration();

        ObjectNodePath fieldNameNodePath = targetFieldMetaData.getFieldNameNodePath();
        ClassMetaModel targetFieldClassMetaModel = targetFieldMetaData.getTargetFieldClassMetaModel();
        ClassMetaModel targetMetaModel = targetFieldMetaData.getTargetFieldClassMetaModel();

        if (isTheSameTypeOrSubType(targetMetaModel, sourceMetaModel)) {
            return fetchValueExpression;
        } else {
            var converterDefinition = genericObjectsConversionService.findConverterDefinition(sourceMetaModel, targetMetaModel);
            if (isEnabledAutoMapping(mapperGenerateConfiguration, mapperConfiguration) && converterDefinition != null) {
                mapperGeneratedCodeMetadata.addConstructorArgument(GenericObjectsConversionService.class);
                String converterName = converterDefinition.getBeanName();
                return String.format("((%s) genericObjectsConversionService.convert(\"%s\", %s))",
                    targetMetaModel.getJavaGenericTypeInfo(), converterName, fetchValueExpression);
            } else if (isEnabledAutoMapping(mapperGenerateConfiguration, mapperConfiguration) &&sourceMetaModel.hasRealClass() && targetMetaModel.hasRealClass()
                && conversionService.canConvert(sourceMetaModel.getRealClass(), targetMetaModel.getRealClass())) {
                mapperGeneratedCodeMetadata.addConstructorArgument(ConversionService.class);
                return String.format("conversionService.convert(%s, %s.class)",
                    fetchValueExpression, targetMetaModel.getCanonicalNameOfRealClass());
            } else if (whenConversionBetweenStringAndEnumMetaModel(sourceMetaModel, targetMetaModel)) {
                return fetchValueExpression;
            } else {

                if (canIgnore(targetFieldMetaData, mapperConfiguration, mapperGenerateConfiguration)) {
                    return new NullAssignExpression(targetFieldClassMetaModel)
                        .generateCodeMetadata(mapperGeneratedCodeMetadata)
                        .getValueGettingCode();
                } else {
                    mapperGeneratedCodeMetadata.addError(createMessagePlaceholder(
                        "mapper.converter.not.found.between.metamodels",
                        sourceMetaModel.getTypeDescription(),
                        targetMetaModel.getTypeDescription(),
                        fieldNameNodePath.getFullPath(),
                        getInMethodPartMessage(methodGeneratorArgument)));
                }

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

    private boolean isEnabledAutoMapping(MapperGenerateConfiguration mapperGenerateConfiguration, MapperConfiguration mapperConfiguration) {
        return mapperGenerateConfiguration.isGlobalEnableAutoMapping() && mapperConfiguration.isEnableAutoMapping();
    }
}
