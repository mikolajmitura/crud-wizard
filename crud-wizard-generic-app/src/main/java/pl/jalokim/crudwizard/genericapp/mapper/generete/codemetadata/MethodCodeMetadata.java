package pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata;

import static pl.jalokim.crudwizard.genericapp.mapper.generete.ClassMetaModelDescribeHelper.getClassModelInfoForGeneratedCode;
import static pl.jalokim.utils.collection.Elements.elements;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.writevalue.WritePropertyStrategy;

@Data
@Builder
public class MethodCodeMetadata {

    String methodReturnType;

    List<String> mappingsCode;
    String lastLine;
    WritePropertyStrategy writePropertyStrategy;

    // below for mapper inner methods
    String methodName;
    String currentNodeType;

    public static class MethodCodeMetadataBuilder {

        List<String> mappingsCode = new ArrayList<>();

        public MethodCodeMetadataBuilder methodName(ClassMetaModel fromType, ClassMetaModel toType) {
            methodName = String.format("map_from_%s_To_%s",
                getClassModelInfoForGeneratedCode(fromType),
                getClassModelInfoForGeneratedCode(toType));
            return this;
        }

        public MethodCodeMetadataBuilder nextMappingCodeLine(String mappingCodeLine) {
            mappingsCode.add(mappingCodeLine);
            return this;
        }
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

}
