package pl.jalokim.crudwizard.genericapp.mapper.conversion;

import static pl.jalokim.utils.collection.Elements.elements;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import pl.jalokim.crudwizard.genericapp.metamodel.classmodel.ClassMetaModel;

@Service
@RequiredArgsConstructor
public class GenericObjectsConversionService {

    private final ApplicationContext applicationContext;

    private final Map<String, ClassMetaModelConverter<Object, Object>> converterByBeanName = new ConcurrentHashMap<>();
    private final Map<String, Map<String, ClassMetaModelConverterDefinition>> genericTypesToGenericTypeConverter = new ConcurrentHashMap<>();
    private final Map<String, Map<Class<?>, ClassMetaModelConverterDefinition>> genericTypesToRawClassConverter = new ConcurrentHashMap<>();
    private final Map<Class<?>, Map<String, ClassMetaModelConverterDefinition>> rawClassesToGenericTypeConverter = new ConcurrentHashMap<>();
    private final Map<Class<?>, Map<Class<?>, ClassMetaModelConverterDefinition>> rawClassesToRawClassConverter = new ConcurrentHashMap<>();

    @PostConstruct
    @SuppressWarnings("unchecked")
    public void init() {
        var classMetaModelConverterDefinitions = elements(applicationContext.getBeanNamesForType(ClassMetaModelConverter.class))
            .map(beanName -> {
                ClassMetaModelConverter<Object, Object> converter = (ClassMetaModelConverter<Object, Object>) applicationContext.getBean(beanName);
                converterByBeanName.put(beanName, converter);
                return new ClassMetaModelConverterDefinition(beanName, converter);
            })
            .asList();

        for (ClassMetaModelConverterDefinition classMetaModelConverterDefinition : classMetaModelConverterDefinitions) {
            ClassMetaModelConverter<Object, Object> converter = classMetaModelConverterDefinition.getConverter();
            ClassMetaModel sourceMetaModel = converter.sourceMetaModel();
            ClassMetaModel targetMetaModel = converter.targetMetaModel();
            if (sourceMetaModel.isGenericModel() && targetMetaModel.isGenericModel()) {
                var convertersByName = genericTypesToGenericTypeConverter
                    .computeIfAbsent(sourceMetaModel.getName(), name -> new ConcurrentHashMap<>());
                convertersByName.put(targetMetaModel.getName(), classMetaModelConverterDefinition);
            } else if (sourceMetaModel.isGenericModel() && targetMetaModel.hasRealClass()) {
                var convertersByRawClass = genericTypesToRawClassConverter
                    .computeIfAbsent(sourceMetaModel.getName(), name -> new ConcurrentHashMap<>());
                convertersByRawClass.put(targetMetaModel.getRealClass(), classMetaModelConverterDefinition);
            } else if (sourceMetaModel.hasRealClass() && targetMetaModel.isGenericModel()) {
                var convertersByName = rawClassesToGenericTypeConverter
                    .computeIfAbsent(sourceMetaModel.getRealClass(), name -> new ConcurrentHashMap<>());
                convertersByName.put(targetMetaModel.getName(), classMetaModelConverterDefinition);
            } else {
                var convertersByRawClass = rawClassesToRawClassConverter
                    .computeIfAbsent(sourceMetaModel.getRealClass(), name -> new ConcurrentHashMap<>());
                convertersByRawClass.put(targetMetaModel.getRealClass(), classMetaModelConverterDefinition);
            }
        }
    }

    public ClassMetaModelConverterDefinition findConverterDefinition(ClassMetaModel sourceMetaModel, ClassMetaModel targetMetaModel) {
        if (sourceMetaModel.isGenericModel() && targetMetaModel.isGenericModel()) {
            var convertersByName = genericTypesToGenericTypeConverter
                .getOrDefault(sourceMetaModel.getName(), Map.of());
            return convertersByName.get(targetMetaModel.getName());
        } else if (sourceMetaModel.isGenericModel() && targetMetaModel.hasRealClass()) {
            var convertersByRawClass = genericTypesToRawClassConverter
                .getOrDefault(sourceMetaModel.getName(), Map.of());
            return convertersByRawClass.get(targetMetaModel.getRealClass());
        } else if (sourceMetaModel.hasRealClass() && targetMetaModel.isGenericModel()) {
            var converterByRawClass = rawClassesToGenericTypeConverter
                .getOrDefault(sourceMetaModel.getRealClass(), Map.of());
            return converterByRawClass.get(targetMetaModel.getName());
        } else {
            var converterByRawClass = rawClassesToRawClassConverter
                .getOrDefault(sourceMetaModel.getRealClass(), Map.of());
            if (converterByRawClass.containsKey(targetMetaModel.getRealClass())) {
                return converterByRawClass.get(targetMetaModel.getRealClass());
            }
        }
        return null;
    }

    public Object convert(String converterName, Object sourceObject) {
        return converterByBeanName.get(converterName).convert(sourceObject);
    }
}
