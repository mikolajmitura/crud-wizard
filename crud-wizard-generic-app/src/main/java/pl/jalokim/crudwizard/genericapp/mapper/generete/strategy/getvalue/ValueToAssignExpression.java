package pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue;

import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MapperCodeMetadata;

public interface ValueToAssignExpression {

    ValueToAssignCodeMetadata generateCodeMetadata(MapperCodeMetadata mapperGeneratedCodeMetadata);
}
