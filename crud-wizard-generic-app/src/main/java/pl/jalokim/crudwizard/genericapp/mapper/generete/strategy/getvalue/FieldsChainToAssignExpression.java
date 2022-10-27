package pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue;

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel.getRawJavaGenericTypeInfoForGenericModel;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelUtils.getRequiredFieldFromClassModel;
import static pl.jalokim.utils.collection.Elements.elements;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import pl.jalokim.crudwizard.core.utils.StringCaseUtils;
import pl.jalokim.crudwizard.genericapp.mapper.generete.FieldMetaResolverConfiguration;
import pl.jalokim.crudwizard.genericapp.mapper.generete.GeneratedLineUtils;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MapperCodeMetadata;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.FieldMetaModel;
import pl.jalokim.utils.reflection.InvokableReflectionUtils;
import pl.jalokim.utils.reflection.MetadataReflectionUtils;
import pl.jalokim.utils.reflection.ReflectionOperationException;

@Value
@AllArgsConstructor
public class FieldsChainToAssignExpression implements ValueToAssignExpression {

    ClassMetaModel sourceMetaModel;
    String valueExpression;
    @EqualsAndHashCode.Exclude
    ValueToAssignExpression parentValueExpression;
    List<FieldMetaModel> fieldChains;

    public static FieldsChainToAssignExpression createFieldsChainToAssignExpression(ValueToAssignExpression parentValueExpression,
        List<FieldMetaModel> fieldChains, MapperCodeMetadata mapperGeneratedCodeMetadata) {

        ValueToAssignCodeMetadata valueToAssignCodeMetadata = parentValueExpression.generateCodeMetadata(mapperGeneratedCodeMetadata);
        String fullValueExpression = valueToAssignCodeMetadata.getFullValueExpression();
        ClassMetaModel returnClassModel = valueToAssignCodeMetadata.getReturnClassModel();
        return new FieldsChainToAssignExpression(returnClassModel, fullValueExpression, parentValueExpression, fieldChains);
    }

    public FieldsChainToAssignExpression(ClassMetaModel sourceMetaModel, String valueExpression, List<FieldMetaModel> fieldChains) {
        this(sourceMetaModel, valueExpression, null, fieldChains);
    }

    public FieldsChainToAssignExpression(ClassMetaModel sourceMetaModel, String valueExpression, FieldMetaModel firstField) {
        this(sourceMetaModel, valueExpression, null, List.of(firstField));
    }

    @Override
    public ValueToAssignCodeMetadata generateCodeMetadata(MapperCodeMetadata mapperGeneratedCodeMetadata) {
        ValueToAssignCodeMetadata returnCodeMetadata = new ValueToAssignCodeMetadata();

        String valueExpressionAsText = valueExpression != null ? valueExpression :
            parentValueExpression.generateCodeMetadata(mapperGeneratedCodeMetadata).getFullValueExpression();

        StringBuilder invokeChain = new StringBuilder(String.format("Optional.ofNullable(%s)", valueExpressionAsText));

        ClassMetaModel currentClassMetaModel = sourceMetaModel;
        for (FieldMetaModel fieldMeta : fieldChains) {
            String fieldName = fieldMeta.getFieldName();
            if (currentClassMetaModel.isGenericModel()) {
                currentClassMetaModel.getRequiredFieldByName(fieldName);
                invokeChain.append(GeneratedLineUtils.wrapWithNextLineWith3Tabs(
                    ".map(genericMap -> ((" + getRawJavaGenericTypeInfoForGenericModel() + ") genericMap).get(\"%s\"))",
                    fieldName));

            } else {
                Class<?> currentRealClass = currentClassMetaModel.getRealClass();
                try {
                    String methodName = "get" + StringCaseUtils.firstLetterToUpperCase(fieldName);
                    MetadataReflectionUtils.getMethod(currentRealClass, methodName);
                    invokeChain.append(GeneratedLineUtils.wrapWithNextLineWith3Tabs(
                        ".map(value -> ((%s) value).%s())", currentRealClass.getCanonicalName(), methodName));
                } catch (ReflectionOperationException ex) {
                    MetadataReflectionUtils.getField(currentRealClass, fieldName);

                    invokeChain.append(GeneratedLineUtils.wrapWithNextLineWith3Tabs(
                        ".map(value -> InvokableReflectionUtils.getValueOfField(value, \"%s\"))", fieldName));
                    mapperGeneratedCodeMetadata.addImport(InvokableReflectionUtils.class);
                }
            }
            currentClassMetaModel = fieldMeta.getFieldType();
        }

        invokeChain.append(GeneratedLineUtils.wrapWithNextLineWith3Tabs(".orElse(null)"));

        returnCodeMetadata.setValueGettingCode(invokeChain.toString());
        returnCodeMetadata.setReturnClassModel(elements(fieldChains).getLast().getFieldType());

        return returnCodeMetadata;
    }

    public FieldsChainToAssignExpression createExpressionWithNextField(String fieldName, FieldMetaResolverConfiguration fieldMetaResolverForRawSource) {
        List<FieldMetaModel> newFieldChains = new ArrayList<>(fieldChains);
        newFieldChains.add(getRequiredFieldFromClassModel(elements(fieldChains).getLast().getFieldType(),
            fieldName, fieldMetaResolverForRawSource));
        return new FieldsChainToAssignExpression(this.sourceMetaModel, this.valueExpression, this.parentValueExpression, newFieldChains);
    }
}
