package pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue;

import static pl.jalokim.utils.collection.Elements.elements;

import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.ConstructorArgument;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValueToAssignCodeMetadata {

    private final Set<ConstructorArgument> constructorArguments = new HashSet<>();
    private final Set<String> staticImports = new HashSet<>();
    private final Set<String> imports = new HashSet<>();

    private String valueGettingCode;
    private ClassMetaModel returnClassModel;

    public void addImport(Class<?> classToImport) {
        imports.add("import " + classToImport.getCanonicalName() + ";");
    }

    public void addStaticImport(String staticImport) {
        staticImports.add("import static" + staticImport + ";");
    }

    public void addConstructorArgument(Class<?> argumentType, String argumentName, String... annotations) {
        constructorArguments.add(ConstructorArgument.builder()
            .argumentType(argumentType)
            .argumentName(argumentName)
            .annotations(elements(annotations).asList())
            .build());
    }

    public String getFullValueExpression() {
        if (returnClassModel != null) {
            return String.format("((%s) %s)", returnClassModel.getJavaGenericTypeInfo(), valueGettingCode);
        }
        return valueGettingCode;
    }
}
