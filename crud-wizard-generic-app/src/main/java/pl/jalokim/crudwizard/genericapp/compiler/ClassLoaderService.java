package pl.jalokim.crudwizard.genericapp.compiler;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelEntityRepository;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperType;

@Service
@RequiredArgsConstructor
public class ClassLoaderService {

    private final Map<String, URLClassLoader> classLoaderByName = new ConcurrentHashMap<>();
    private final CompiledCodeRootPathProvider compiledCodeRootPathProvider;
    private final MapperMetaModelEntityRepository mapperMetaModelEntityRepository;

    public void createClassLoaders() {
        Set<String> classLoaderNames = new HashSet<>();
        mapperMetaModelEntityRepository.findAllByMapperType(MapperType.GENERATED)
            .forEach(mapperMetaModelEntity -> {
                var mapperGenerateConfiguration = mapperMetaModelEntity.getMapperGenerateConfiguration();
                var mapperCompiledCodeMetadata = mapperGenerateConfiguration.getMapperCompiledCodeMetadata();
                classLoaderNames.add(mapperCompiledCodeMetadata.getSessionGenerationTimestamp());
            });
        classLoaderNames.forEach(this::createNewClassLoader);
    }

    @SneakyThrows
    public void createNewClassLoader(String classLoaderName) {
        File classLoaderPath = new File(compiledCodeRootPathProvider.getCompiledCodeRootPath() + "/" + classLoaderName);
        URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] { classLoaderPath.toURI().toURL() });
        classLoaderByName.put(classLoaderName, classLoader);
    }

    @SneakyThrows
    public Class<?> loadClass(String className, String classLoaderName) {
        return Class.forName(className, true, classLoaderByName.get(classLoaderName));
    }
}
