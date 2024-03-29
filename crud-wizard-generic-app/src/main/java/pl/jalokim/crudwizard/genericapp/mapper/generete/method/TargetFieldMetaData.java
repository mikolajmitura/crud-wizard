package pl.jalokim.crudwizard.genericapp.mapper.generete.method;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.PropertiesOverriddenMapping;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ValueToAssignExpression;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.service.translator.ObjectNodePath;

@Value
@Builder
public class TargetFieldMetaData {

    String fieldName;
    ObjectNodePath fieldNameNodePath;
    ClassMetaModel targetFieldClassMetaModel;

    @Builder.Default
    List<ValueToAssignExpression> overriddenPropertyStrategiesByForField = new ArrayList<>();

    @Builder.Default
    PropertiesOverriddenMapping propertiesOverriddenMappingForField = PropertiesOverriddenMapping.builder().build();
}
