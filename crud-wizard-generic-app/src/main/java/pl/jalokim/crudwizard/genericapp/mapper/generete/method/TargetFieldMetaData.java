package pl.jalokim.crudwizard.genericapp.mapper.generete.method;

import java.util.List;
import lombok.Builder;
import lombok.Value;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.crudwizard.genericapp.mapper.generete.config.PropertiesOverriddenMapping;
import pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue.ValueToAssignExpression;
import pl.jalokim.crudwizard.genericapp.service.translator.ObjectNodePath;

@Value
@Builder
public class TargetFieldMetaData {

    String fieldName;
    ObjectNodePath fieldNameNodePath;
    ClassMetaModel targetFieldClassMetaModel;
    List<ValueToAssignExpression> overriddenPropertyStrategiesByForField;
    PropertiesOverriddenMapping propertiesOverriddenMappingForField;
}
