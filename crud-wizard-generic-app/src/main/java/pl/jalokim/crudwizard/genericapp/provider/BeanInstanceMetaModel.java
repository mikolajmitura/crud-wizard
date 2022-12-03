package pl.jalokim.crudwizard.genericapp.provider;

import java.util.List;
import lombok.Builder;
import lombok.Value;
import pl.jalokim.crudwizard.genericapp.metamodel.method.BeanAndMethodMetaModel;

@Value
@Builder
public class BeanInstanceMetaModel {

    String beanName;
    String className;
    Object beanInstance;
    List<BeanAndMethodMetaModel> genericMethodMetaModels;
}
