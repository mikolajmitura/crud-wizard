package pl.jalokim.crudwizard.genericapp.provider;

import static java.util.Collections.unmodifiableList;
import static pl.jalokim.crudwizard.core.utils.ClassUtils.clearCglibClassName;
import static pl.jalokim.utils.collection.Elements.elements;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import pl.jalokim.crudwizard.core.utils.ClassUtils;
import pl.jalokim.crudwizard.genericapp.config.GenericMapper;
import pl.jalokim.crudwizard.genericapp.config.GenericMethod;
import pl.jalokim.crudwizard.genericapp.config.GenericService;
import pl.jalokim.crudwizard.genericapp.service.invoker.BeanMethodMetaModelCreator;

@Service
@RequiredArgsConstructor
public class GenericBeansProvider {

    private final AtomicReference<List<BeanInstanceMetaModel>> allGenericMapperBeansReference = new AtomicReference<>();
    private final AtomicReference<List<BeanInstanceMetaModel>> allGenericServiceBeansReference = new AtomicReference<>();

    private final ApplicationContext applicationContext;
    private final BeanMethodMetaModelCreator beanMethodMetaModelCreator;

    public List<BeanInstanceMetaModel> getAllGenericMapperBeans() {
        return Optional.ofNullable(allGenericMapperBeansReference.get())
            .orElseGet(() -> {
                allGenericMapperBeansReference.set(findAllGenericBeans(GenericMapper.class));
                return allGenericMapperBeansReference.get();
            });
    }

    public List<BeanInstanceMetaModel> getAllGenericServiceBeans() {
        return Optional.ofNullable(allGenericServiceBeansReference.get())
            .orElseGet(() -> {
                allGenericServiceBeansReference.set(findAllGenericBeans(GenericService.class));
                return allGenericServiceBeansReference.get();
            });
    }

    public List<BeanInstanceMetaModel> findAllGenericBeans(Class<? extends Annotation> annotationType) {
        List<BeanInstanceMetaModel> genericInstanceBeanMetaModel = new ArrayList<>();
        Map<String, Object> genericMappers = applicationContext.getBeansWithAnnotation(annotationType);
        for (var genericMapperEntry : genericMappers.entrySet()) {
            Object beanInstance = genericMapperEntry.getValue();
            String genericInstanceClassName = beanInstance.getClass().getCanonicalName();
            String realClassName = clearCglibClassName(genericInstanceClassName);
            Class<?> realClass = ClassUtils.loadRealClass(genericInstanceClassName);

            genericInstanceBeanMetaModel.add(BeanInstanceMetaModel.builder()
                .beanInstance(beanInstance)
                .className(realClassName)
                .beanName(genericMapperEntry.getKey())
                .genericMethodMetaModels(
                    elements(realClass.getMethods())
                        .filter(declaredMethod -> declaredMethod.isAnnotationPresent(GenericMethod.class))
                        .map(declaredMethod -> beanMethodMetaModelCreator.createBeanMethodMetaModel(declaredMethod, realClass))
                        .asList()
                )
                .build());
        }
        return unmodifiableList(genericInstanceBeanMetaModel);
    }
}
