package pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata;

import static pl.jalokim.utils.collection.Elements.elements;

import lombok.RequiredArgsConstructor;
import pl.jalokim.utils.template.TemplateAsText;

@RequiredArgsConstructor
public class EnumsMappingMethodResolver implements MethodTemplateResolver {

    private final String whenNotMappedEnum;

    @Override
    public String generateMethodCode(MethodCodeMetadata methodCodeMetadata) {
        return TemplateAsText.fromClassPath("templates/mapper/enums-mapping-method-template", true)
            .overrideVariable("methodReturnType", methodCodeMetadata.getMethodReturnType())
            .overrideVariable("methodName", methodCodeMetadata.getMethodName())
            .overrideVariable("sourceObjectType", methodCodeMetadata.getMethodArguments().get(0)
                .getArgumentType().getJavaGenericTypeInfo())
            .overrideVariable("cases", elements(methodCodeMetadata.getMethodCodeLines()).concatWithNewLines())
            .overrideVariable("whenNotMappedEnum", whenNotMappedEnum)
            .getCurrentTemplateText();
    }
}
