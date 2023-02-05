package pl.jalokim.crudwizard.genericapp.mapper.generete.strategy.getvalue;

import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.ClassMetaModelFactory.createClassMetaModel;
import static pl.jalokim.crudwizard.genericapp.metamodel.classmodel.utils.fieldresolver.FieldMetaResolverConfiguration.DEFAULT_FIELD_RESOLVERS_CONFIG;

import java.lang.reflect.Method;
import java.util.List;
import lombok.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import pl.jalokim.crudwizard.genericapp.mapper.generete.codemetadata.MapperCodeMetadata;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;
import pl.jalokim.utils.collection.Elements;
import pl.jalokim.utils.reflection.MetadataReflectionUtils;

@Value
public class BySpringBeanMethodAssignExpression implements ValueToAssignExpression {

    Class<?> beanType;
    String beanName;
    String methodName;
    List<ValueToAssignExpression> methodArguments;

    @Override
    public ValueToAssignCodeMetadata generateCodeMetadata(MapperCodeMetadata mapperGeneratedCodeMetadata) {
        ValueToAssignCodeMetadata returnCodeMetadata = new ValueToAssignCodeMetadata();

        List<ValueToAssignCodeMetadata> returnArgumentCodesMeta = Elements.elements(methodArguments)
            .map(argument -> argument.generateCodeMetadata(mapperGeneratedCodeMetadata))
            .asList();

        List<? extends Class<?>> classes = Elements.elements(returnArgumentCodesMeta)
            .map(ValueToAssignCodeMetadata::getReturnClassModel)
            .map(ClassMetaModel::getRealClass)
            .asList();

        Method method = MetadataReflectionUtils.getMethod(beanType, methodName, classes.toArray(new Class[0]));

        returnCodeMetadata.setReturnClassModel(createClassMetaModel(method.getGenericReturnType(), DEFAULT_FIELD_RESOLVERS_CONFIG));

        mapperGeneratedCodeMetadata.addConstructorArgument(beanType, beanName, "@" + Qualifier.class.getCanonicalName() + "(\"" + beanName + "\")");

        String arguments = Elements.elements(returnArgumentCodesMeta)
            .map(ValueToAssignCodeMetadata::getFullValueExpression)
            .asConcatText(", ");

        returnCodeMetadata.setValueGettingCode(String.format("%s.%s(%s)", beanName, methodName, arguments));

        return returnCodeMetadata;
    }
}
