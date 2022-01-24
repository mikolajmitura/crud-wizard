package pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue;

import static pl.jalokim.utils.collection.Elements.elements;

import java.util.List;
import lombok.Value;
import pl.jalokim.crudwizard.core.exception.TechnicalException;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.core.metamodels.FieldMetaModel;
import pl.jalokim.crudwizard.core.utils.StringCaseUtils;
import pl.jalokim.crudwizard.genericapp.mapper.generete.GeneratedLineUtils;
import pl.jalokim.utils.reflection.InvokableReflectionUtils;
import pl.jalokim.utils.reflection.MetadataReflectionUtils;
import pl.jalokim.utils.reflection.ReflectionOperationException;

@Value
public class FromFieldsChainStrategy implements PropertyValueMappingStrategy {

    ClassMetaModel sourceMetaModel;
    String rootObjectVariableName;
    List<FieldMetaModel> fieldChains;

    @Override
    public GetPropertyCodeMetadata generateReturnCodeMetadata() {
        GetPropertyCodeMetadata returnCodeMetadata = new GetPropertyCodeMetadata();
        StringBuilder invokeChain = new StringBuilder(String.format("Optional.ofNullable(%s)", rootObjectVariableName));

        ClassMetaModel currentClassMetaModel = sourceMetaModel;
        for (FieldMetaModel fieldMeta : fieldChains) {
            String fieldName = fieldMeta.getFieldName();
            if (currentClassMetaModel.isGenericClassModel()) {
                FieldMetaModel fieldByName = currentClassMetaModel.getFieldByName(fieldName);
                if (fieldByName == null) {
                    throw new TechnicalException("cannot find field" + fieldName + " in meta model named: " + currentClassMetaModel.getName());
                }
                invokeChain.append(GeneratedLineUtils.wrapWithNextLineWith3Tabs(
                    ".map(genericMap -> ((Map<String, Object>) genericMap).get(\"%s\"))",
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
                    returnCodeMetadata.addImport(InvokableReflectionUtils.class);
                }
            }
            currentClassMetaModel = fieldMeta.getFieldType();
        }

        invokeChain.append(GeneratedLineUtils.wrapWithNextLineWith3Tabs(".orElse(null)"));

        returnCodeMetadata.setValueGettingCode(invokeChain.toString());
        returnCodeMetadata.setReturnClassModel(elements(fieldChains).getLast().getFieldType());

        return returnCodeMetadata;
    }
}
