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
import pl.jalokim.crudwizard.genericapp.mapper.generete.MapperArgumentMethodModel;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.writevalue.WritePropertyStrategy;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;

@Data
@Builder(toBuilder = true)
public class MethodCodeMetadata {

    String methodReturnType;
    ClassMetaModel returnClassMetaModel;

    List<String> methodCodeLines;
    String lastLine;
    WritePropertyStrategy writePropertyStrategy;

    // below for mapper inner methods
    String methodName;
    List<MapperArgumentMethodModel> methodArguments;

    @Builder.Default
    AtomicReference<String> generatedCode = new AtomicReference<>();

    MethodCodeMetadata parentMethodMetadata;

    @Builder.Default
    MethodTemplateResolver methodTemplateResolver = new InnerGenericMappingMethodResolver();

    boolean generated;

    @Builder.Default
    Map<String, MethodCodeMetadata> childMethodsByGeneratedCode = new HashMap<>();

    public void setMethodName(String methodName) {
        this.methodName = methodName;
        generatedCode.set(null);
    }

    public static class MethodCodeMetadataBuilder {

        List<String> methodCodeLines = new ArrayList<>();

        public MethodCodeMetadataBuilder nextMappingCodeLine(String mappingCodeLine) {
            methodCodeLines.add(mappingCodeLine);
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
        return elements(methodCodeLines)
            .mapWithIndexed(indexedElement -> {
                if (writePropertyStrategy != null && indexedElement.isLast()) {
                    return writePropertyStrategy.lastWritePropertyLineChanger(indexedElement.getValue());
                }
                return indexedElement.getValue();
            }).concatWithNewLines();
    }

    public String generateCodeForMethod() {
        if (generatedCode.get() == null) {
            generatedCode.set(methodTemplateResolver.generateMethodCode(this));
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

    String buildMethodArguments() {
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
