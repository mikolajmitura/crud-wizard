package pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValueToAssignCodeMetadata {

    private String valueGettingCode;
    private ClassMetaModel returnClassModel;

    public String getFullValueExpression() {
        if (returnClassModel != null) {
            return String.format("((%s) %s)", returnClassModel.getJavaGenericTypeInfo(), valueGettingCode);
        }
        return valueGettingCode;
    }
}
