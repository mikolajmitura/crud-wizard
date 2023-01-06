package pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata;

import static pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.ClassMetamodelDescriber.getFullDescription;

import pl.jalokim.utils.template.TemplateAsText;

public class InnerGenericMappingMethodResolver implements MethodTemplateResolver {

    @Override
    public String generateMethodCode(MethodCodeMetadata methodCodeMetadata) {
        return TemplateAsText.fromClassPath("templates/mapper/mapper-method-template", true)
            .overrideVariable("isGenerated", methodCodeMetadata.getGeneratedLine())
            .overrideVariable("methodReturnType", methodCodeMetadata.getMethodReturnType())
            .overrideVariable("methodName", methodCodeMetadata.getMethodName())
            .overrideVariable("methodArguments", methodCodeMetadata.buildMethodArguments())
            .overrideVariable("mappingsCode", methodCodeMetadata.getMappingsCodeAsText())
            .overrideVariable("lastLine", methodCodeMetadata.getLastLine())
            .overrideVariable("sourceMetaModel", getFullDescription(methodCodeMetadata.getMethodArguments()))
            .overrideVariable("targetMetaModel", getFullDescription(methodCodeMetadata.getReturnClassMetaModel()))
            .overrideVariable("earlierNullReturnExpression", methodCodeMetadata.getMethodArguments().size() == 1 ?
                TemplateAsText.fromClassPath("templates/mapper/earlier-null-return").getCurrentTemplateText() : "")
            .getCurrentTemplateText();
    }
}
