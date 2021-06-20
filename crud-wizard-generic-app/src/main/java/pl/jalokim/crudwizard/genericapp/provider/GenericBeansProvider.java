package pl.jalokim.crudwizard.genericapp.provider;

import static java.util.Collections.unmodifiableList;
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
import pl.jalokim.crudwizard.core.metamodels.BeanMethodMetaModel;
import pl.jalokim.crudwizard.genericapp.config.GenericMapper;
import pl.jalokim.crudwizard.genericapp.config.GenericMethod;
import pl.jalokim.crudwizard.genericapp.config.GenericService;

@Service
@RequiredArgsConstructor
public class GenericBeansProvider {

    private final AtomicReference<List<BeanInstanceMetaModel>> allGenericMapperBeansReference = new AtomicReference<>();
    private final AtomicReference<List<BeanInstanceMetaModel>> allGenericServiceBeansReference = new AtomicReference<>();

    private final ApplicationContext applicationContext;

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
            Class<?> mapperClass = genericMapperEntry.getValue().getClass();
            genericInstanceBeanMetaModel.add(BeanInstanceMetaModel.builder()
                .beanInstance(genericMapperEntry.getValue())
                .className(genericMapperEntry.getValue().getClass().getCanonicalName())
                .beanName(genericMapperEntry.getKey())
                .genericMethodMetaModels(
                    elements(mapperClass.getDeclaredMethods())
                        .filter(declaredMethod -> declaredMethod.isAnnotationPresent(GenericMethod.class))
                        .map(declaredMethod -> BeanMethodMetaModel.builder()
                            // TODO load method meta model info
                            .name(declaredMethod.getName())
                            .build())
                        .asList()
                )
                .build());
        }
        return unmodifiableList(genericInstanceBeanMetaModel);
    }
}
