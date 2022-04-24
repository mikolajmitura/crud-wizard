package pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata;

import static pl.jalokim.crudwizard.genericapp.mapper.generete.ClassMetaModelForMapperHelper.getClassModelInfoForGeneratedCode;
import static pl.jalokim.utils.collection.Elements.bySplitText;
import static pl.jalokim.utils.collection.Elements.elements;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.mapper.generete.MapperArgumentMethodModel;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.writevalue.WritePropertyStrategy;
import pl.jalokim.utils.template.TemplateAsText;

@Data
@Builder(toBuilder = true)
public class MethodCodeMetadata {

    String methodReturnType;
    ClassMetaModel returnClassMetaModel;

    List<String> mappingsCode;
    String lastLine;
    WritePropertyStrategy writePropertyStrategy;

    // below for mapper inner methods
    String methodName;
    List<MapperArgumentMethodModel> methodArguments;

    @Builder.Default
    AtomicReference<String> generatedCode = new AtomicReference<>();

    MethodCodeMetadata parentMethodMetadata;

    boolean generated;

    @Builder.Default
    Map<String, MethodCodeMetadata> childMethodsByGeneratedCode = new HashMap<>();

    public void setMethodName(String methodName) {
        this.methodName = methodName;
        generatedCode.set(null);
    }

    public static class MethodCodeMetadataBuilder {

        List<String> mappingsCode = new ArrayList<>();

        public MethodCodeMetadataBuilder nextMappingCodeLine(String mappingCodeLine) {
            mappingsCode.add(mappingCodeLine);
            return this;
        }
    }

    public static String createMethodName(List<MapperArgumentMethodModel> fromTypes, ClassMetaModel toType) {
        return String.format("map%sTo%s",
            getClassModelInfoForGeneratedCode(fromTypes),
            getClassModelInfoForGeneratedCode(toType));
    }

    public static String regenerateMethodName(String newMethodName, Set<String> methodNames) {
        if (!methodNames.contains(newMethodName)) {
            return newMethodName;
        }

        String methodNameWithTheHighestIndex = elements(methodNames)
            .filter(methodName -> methodName.contains(newMethodName))
            .sorted(Comparator.comparing(methodName -> {
                String lastPart = bySplitText(methodName, "_").getLastOrNull();
                try {
                    return Integer.parseInt(lastPart);
                } catch (NumberFormatException ex) {
                    return 0;
                }
            }))
            .getLastOrNull();

        String lastPart = bySplitText(methodNameWithTheHighestIndex, "_").getLastOrNull();
        int lastIndex;
        try {
            lastIndex = Integer.parseInt(lastPart);
        } catch (NumberFormatException ex) {
            lastIndex = 0;
        }
        return newMethodName + "_" + ++lastIndex;
    }

    public String getMappingsCodeAsText() {
        return elements(mappingsCode)
            .mapWithIndexed(indexedElement -> {
                if (writePropertyStrategy != null && indexedElement.isLast()) {
                    return writePropertyStrategy.lastWritePropertyLineChanger(indexedElement.getValue());
                }
                return indexedElement.getValue();
            }).concatWithNewLines();
    }

    public String generateCodeForMethod() {
        if (generatedCode.get() == null) {
            generatedCode.set(TemplateAsText.fromClassPath("templates/mapper/mapper-method-template", true)
                .overrideVariable("isGenerated", getGeneratedLine())
                .overrideVariable("methodReturnType", getMethodReturnType())
                .overrideVariable("methodName", getMethodName())
                .overrideVariable("methodArguments", buildMethodArguments())
                .overrideVariable("mappingsCode", getMappingsCodeAsText())
                .overrideVariable("lastLine", getLastLine())
                .overrideVariable("earlierNullReturnExpression", getMethodArguments().size() == 1 ?
                    TemplateAsText.fromClassPath("templates/mapper/earlier-null-return").getCurrentTemplateText() : "")
                .getCurrentTemplateText());
            return generatedCode.get();
        }

        return generatedCode.get();
    }

    public boolean hasTheSameChildMethods(MethodCodeMetadata otherMethodCodeMetadata) {
        return childMethodsByGeneratedCode.equals(otherMethodCodeMetadata.getChildMethodsByGeneratedCode());
    }

    public void addChildMethod(MethodCodeMetadata otherMethodCodeMetadata) {
        childMethodsByGeneratedCode.putIfAbsent(otherMethodCodeMetadata.generateCodeForMethod(), otherMethodCodeMetadata);
    }

    private String buildMethodArguments() {
        if (CollectionUtils.isEmpty(getMethodArguments())) {
            return StringUtils.EMPTY;
        }
        return ", " + elements(getMethodArguments())
            .map(methodArgument -> methodArgument.getArgumentType().getJavaGenericTypeInfo() + " " + methodArgument.getArgumentName())
            .asConcatText(", ");
    }

    @Override
    public String toString() {
        return "methodName: " + methodName;
    }

    public String getGeneratedLine() {
        return isGenerated() ? "    @GeneratedMethod" : "";
    }
}
