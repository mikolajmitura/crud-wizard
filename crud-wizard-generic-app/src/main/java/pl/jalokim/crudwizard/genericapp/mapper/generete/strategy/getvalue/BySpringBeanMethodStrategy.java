package pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue;

import java.lang.reflect.Method;
import java.util.List;
import lombok.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;
import pl.jalokim.utils.collection.Elements;
import pl.jalokim.utils.reflection.MetadataReflectionUtils;

@Value
public class BySpringBeanMethodStrategy implements PropertyValueMappingStrategy {

    Class<?> beanType;
    String beanName;
    String methodName;
    List<PropertyValueMappingStrategy> methodArguments;

    @Override
    public GetPropertyCodeMetadata generateReturnCodeMetadata() {
        GetPropertyCodeMetadata returnCodeMetadata = new GetPropertyCodeMetadata();

        List<GetPropertyCodeMetadata> returnArgumentCodesMeta = Elements.elements(methodArguments)
            .map(PropertyValueMappingStrategy::generateReturnCodeMetadata)
            .asList();

        List<? extends Class<?>> classes = Elements.elements(returnArgumentCodesMeta)
            .map(GetPropertyCodeMetadata::getReturnClassModel)
            .map(ClassMetaModel::getRealClass)
            .asList();

        Method method = MetadataReflectionUtils.getMethod(beanType, methodName, classes.toArray(new Class[0]));

        returnCodeMetadata.setReturnClassModel(ClassMetaModel.builder()
            .realClass(method.getReturnType())
            .build());

        returnCodeMetadata.addConstructorArgument(beanType, beanName, "@" + Qualifier.class.getCanonicalName() + "(\"" + beanName + "\")");

        String arguments = Elements.elements(returnArgumentCodesMeta)
            .map(GetPropertyCodeMetadata::getFullValueExpression)
            .asConcatText(", ");

        returnCodeMetadata.setValueGettingCode(String.format("%s.%s(%s);", beanName, methodName, arguments));

        return returnCodeMetadata;
    }
}
