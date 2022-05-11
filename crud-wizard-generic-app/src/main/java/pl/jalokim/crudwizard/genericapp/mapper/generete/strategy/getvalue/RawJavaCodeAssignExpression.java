package pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MapperCodeMetadata;

@RequiredArgsConstructor
@Data
public class RawJavaCodeAssignExpression implements ValueToAssignExpression {

    private final ClassMetaModel returnClassMetaModel;
    private final String rawJavaCode;

    @Override
    public ValueToAssignCodeMetadata generateCodeMetadata(MapperCodeMetadata mapperGeneratedCodeMetadata) {
        return ValueToAssignCodeMetadata.builder()
            .valueGettingCode(rawJavaCode)
            .returnClassModel(returnClassMetaModel)
            .build();
    }

    public static RawJavaCodeAssignExpression createRawJavaCodeExpression(ClassMetaModel returnClassMetaModel, String rawJavaCode) {
        return new RawJavaCodeAssignExpression(returnClassMetaModel, rawJavaCode);
    }
}
