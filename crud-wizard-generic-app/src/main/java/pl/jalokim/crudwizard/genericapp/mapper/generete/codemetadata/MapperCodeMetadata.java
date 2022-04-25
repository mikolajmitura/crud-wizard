package pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata;

import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.template.TemplateAsText.fromText;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import lombok.Data;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.core.translations.MessagePlaceholder;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ValueToAssignCodeMetadata;
import pl.jalokim.crudwizard.genericapp.mapper.generete.validation.MapperValidationContext;

@Data
public class MapperCodeMetadata {

    String mapperClassName;
    Set<String> imports = new HashSet<>();
    Set<String> staticImports = new HashSet<>();
    Set<ConstructorArgument> constructorArguments = new HashSet<>();
    MethodCodeMetadata mainMethodCodeMetadata;
    List<MethodCodeMetadata> otherMethods = new ArrayList<>();
    Set<String> methodNames = new HashSet<>();
    MapperValidationContext mapperValidationContext = new MapperValidationContext();

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

    public void addOtherMethod(MethodCodeMetadata otherMethod) {
        methodNames.add(otherMethod.getMethodName());
        otherMethods.add(otherMethod);
    }

    public void fetchMetaDataFrom(ValueToAssignCodeMetadata getPropertyCodeMetadata) {
        imports.addAll(getPropertyCodeMetadata.getImports());
        staticImports.addAll(getPropertyCodeMetadata.getStaticImports());
        constructorArguments.addAll(getPropertyCodeMetadata.getConstructorArguments());
    }

    public String getImportsAsText() {
        return elements(imports)
            .sorted()
            .concatWithNewLines();
    }

    public String getStaticImportsAsText() {
        return elements(staticImports)
            .sorted()
            .concatWithNewLines();
    }

    public String getFieldsAsText() {
        return elements(constructorArguments)
            .sorted(Comparator.comparing(ConstructorArgument::getArgumentName))
            .map(argument -> String.format("\t\tprivate final %s %s;",
                argument.getArgumentTypeAsText(),
                argument.getArgumentName()))
            .concatWithNewLines();
    }

    public String getConstructorArgumentsAsText() {
        return elements(constructorArguments)
            .sorted(Comparator.comparing(ConstructorArgument::getArgumentName))
            .map(argument -> String.format("%s%s %s",
                elements(argument.getAnnotations()).asConcatText(" "),
                argument.getArgumentTypeAsText(),
                argument.getArgumentName()))
            .asConcatText("," + System.lineSeparator());
    }

    public String getFieldsAssignmentsAsText() {
        return elements(constructorArguments)
            .sorted(Comparator.comparing(ConstructorArgument::getArgumentName))
            .map(argument -> fromText("\t\tthis.${varName} = ${varName};")
                .overrideVariable("varName", argument.getArgumentName())
                .getCurrentTemplateText())
            .concatWithNewLines();
    }

    public String getOtherMapperMethodsAsText() {
        return elements(otherMethods)
            .sorted(Comparator.comparing(MethodCodeMetadata::getMethodName))
            .map(MethodCodeMetadata::generateCodeForMethod
            ).concatWithNewLines();
    }

    public MethodCodeMetadata getMethodWhenExistsWithTheSameCode(MethodCodeMetadata methodCodeMetadata) {
        String generateCodeForMethod = methodCodeMetadata.generateCodeForMethod();
        return elements(otherMethods)
            .filter(otherMethod -> otherMethod.generateCodeForMethod().equals(generateCodeForMethod))
            .getFirstOrNull();
    }

    public void addError(MessagePlaceholder messagePlaceholder) {
        mapperValidationContext.addError(messagePlaceholder);
    }

    public void throwMappingError(MessagePlaceholder messagePlaceholder) {
        throw new MappingException(messagePlaceholder);
    }

    public void checkValidationResults() {
        mapperValidationContext.checkValidationResults();
    }

    public List<MethodCodeMetadata> findMatchNotGeneratedMethod(ClassMetaModel targetClassMetaModel, ClassMetaModel sourceClassMetaModel) {
        return elements(otherMethods)
            .filter(Predicate.not(MethodCodeMetadata::isGenerated))
            .filter(methodCodeMetadata ->
                methodCodeMetadata.getMethodArguments().size() == 1
                    && methodCodeMetadata.getMethodArguments().get(0).getArgumentType().isTheSameMetaModel(sourceClassMetaModel)
                    && methodCodeMetadata.getReturnClassMetaModel().isTheSameMetaModel(targetClassMetaModel)
            )
            .asList();
    }
}
