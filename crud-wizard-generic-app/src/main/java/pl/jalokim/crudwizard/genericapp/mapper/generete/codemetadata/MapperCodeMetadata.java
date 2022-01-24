package pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata;

import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.template.TemplateAsText.fromText;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Data;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.GetPropertyCodeMetadata;
import pl.jalokim.utils.template.TemplateAsText;

@Data
public class MapperCodeMetadata {

    String mapperClassName;
    Set<String> imports = new HashSet<>();
    Set<String> staticImports = new HashSet<>();
    Set<ConstructorArgument> constructorArguments = new HashSet<>();
    MethodCodeMetadata mainMethodCodeMetadata;
    List<MethodCodeMetadata> otherMethods = new ArrayList<>();

    public void addImport(Class<?> classToImport) {
        imports.add("import " + classToImport.getCanonicalName() + ";");
    }

    public void addStaticImport(String staticImport) {
        staticImports.add("import static" + staticImport + ";");
    }

    public void addConstructorArgument(Class<?> constructorArgumentType) {
        addConstructorArgument(ConstructorArgument.newConstructorArgument(constructorArgumentType));
    }

    public void addConstructorArgument(ConstructorArgument constructorArgument) {
        constructorArguments.add(constructorArgument);
    }

    public void fetchMetaDataFrom(GetPropertyCodeMetadata getPropertyCodeMetadata) {
        imports.addAll(getPropertyCodeMetadata.getImports());
        staticImports.addAll(getPropertyCodeMetadata.getStaticImports());
        constructorArguments.addAll(getPropertyCodeMetadata.getConstructorArguments());
    }

    public String getImportsAsText() {
        return elements(imports).concatWithNewLines();
    }

    public String getStaticImportsAsText() {
        return elements(staticImports).concatWithNewLines();
    }

    public String getFieldsAsText() {
        return elements(constructorArguments)
            .map(argument -> String.format("\t\tprivate final %s %s;",
                argument.getArgumentTypeAsText(),
                argument.getArgumentName()))
            .concatWithNewLines();
    }

    public String getConstructorArgumentsAsText() {
        return elements(constructorArguments)
            .map(argument -> String.format("%s%s %s",
                elements(argument.getAnnotations()).asConcatText(" "),
                argument.getArgumentTypeAsText(),
                argument.getArgumentName()))
            .asConcatText("," + System.lineSeparator());
    }

    public String getFieldsAssignmentsAsText() {
        return elements(constructorArguments)
            .map(argument -> fromText("\t\tthis.${varName} = ${varName};")
                .overrideVariable("varName", argument.getArgumentName())
                .getCurrentTemplateText())
            .concatWithNewLines();
    }

    public String getOtherMapperMethodsAsText() {
        return elements(otherMethods)
            .map(otherMethod -> TemplateAsText.fromClassPath("templates/mapper/mapper-method-template")
                .overrideVariable("methodReturnType", otherMethod.getMethodReturnType())
                .overrideVariable("methodName", otherMethod.getMethodName())
                .overrideVariable("currentNodeType", otherMethod.getCurrentNodeType())
                .overrideVariable("mappingsCode", otherMethod.getMappingsCodeAsText())
                .overrideVariable("lastLine", otherMethod.getLastLine())
            ).concatWithNewLines();
    }
}
