package pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata;

import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.template.TemplateAsText.fromText;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.core.translations.MessagePlaceholder;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.MapperGenerateConfiguration;
import pl.jalokim.crudwizard.genericapp.mapper.generete.method.MapperMethodGenerator;
import pl.jalokim.crudwizard.genericapp.mapper.generete.method.MethodMetadataMapperConfig;
import pl.jalokim.crudwizard.genericapp.mapper.generete.validation.MapperValidationContext;

@Data
@RequiredArgsConstructor
public class MapperCodeMetadata {

    private final MapperMethodGenerator mapperMethodGenerator;
    private final MapperGenerateConfiguration mapperGenerateConfiguration;

    private String mapperClassName;
    private Set<String> imports = new HashSet<>();
    private Set<String> staticImports = new HashSet<>();
    private Set<ConstructorArgument> constructorArguments = new LinkedHashSet<>();
    private MethodCodeMetadata mainMethodCodeMetadata;
    private List<MethodCodeMetadata> otherMethods = new ArrayList<>();
    private List<MethodMetadataMapperConfig> otherMethodsFromConfig = new ArrayList<>();
    private Set<String> methodNames = new HashSet<>();
    private MapperValidationContext mapperValidationContext = new MapperValidationContext();

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

    public void addConstructorArgument(Class<?> argumentType, String argumentName, String... annotations) {
        constructorArguments.add(ConstructorArgument.builder()
            .argumentType(argumentType)
            .argumentName(argumentName)
            .annotations(elements(annotations).asList())
            .build());
    }

    public void addOtherMethod(MethodCodeMetadata otherMethod) {
        methodNames.add(otherMethod.getMethodName());
        otherMethods.add(otherMethod);
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

    public List<MethodMetadataMapperConfig> findMatchNotGeneratedMethod(ClassMetaModel targetClassMetaModel, ClassMetaModel sourceClassMetaModel) {
        return elements(otherMethodsFromConfig)
            .filter(methodCodeMetadata ->
                    methodCodeMetadata.getArgumentClassMetaModel().isTheSameMetaModel(sourceClassMetaModel)
                    && methodCodeMetadata.getReturnClassMetaModel().isTheSameMetaModel(targetClassMetaModel)
            )
            .asList();
    }

    public MethodMetadataMapperConfig getMethodByName(String innerMethodName) {
        return elements(otherMethodsFromConfig)
            .filter(method -> method.getMethodName().equals(innerMethodName))
            .getFirstOrNull();
    }


}
