package pl.jalokim.crudwizard.genericapp.mapper.generete.parser;

import static pl.jalokim.crudwizard.core.translations.MessagePlaceholder.createMessagePlaceholder;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.utils.ClassUtils;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextService;

@Component
@RequiredArgsConstructor
public class ClassMetaModelMapperParser {

    private final MetaModelContextService metaModelContextService;

    public ClassMetaModel parseClassMetaModel(String text, MapperConfigurationParserContext parserContext) {
        text = text.trim();
        String[] textParts = text.split("_");
        String metaModelType = textParts[0];
        String modeModelName = textParts[1];
        if (metaModelType.equals("c")) {
            try {
                Class<?> realClass = ClassUtils.loadRealClass(modeModelName);
                return ClassMetaModel.builder()
                    .realClass(realClass)
                    .build();
            } catch (Exception ex) {
                parserContext.throwParseException(createMessagePlaceholder("mapper.parser.class.not.found", modeModelName));
            }
        } else if (metaModelType.equals("m")) {
            ClassMetaModel classMetaModelByName = metaModelContextService.getClassMetaModelByName(modeModelName);
            if (classMetaModelByName == null) {
                parserContext.throwParseException(createMessagePlaceholder("mapper.parser.meta.model.not.found", modeModelName));
            }
            return classMetaModelByName;
        }
        return null;
    }
}
