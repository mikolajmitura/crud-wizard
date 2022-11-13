package pl.jalokim.crudwizard.genericapp.compiler;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClassLoaderService {

    private final Map<String, URLClassLoader> classLoaderByName = new ConcurrentHashMap<>();
    private final CompiledCodeRootPathProvider compiledCodeRootPathProvider;

    // TODO #1 load classes from other classloader after restart application.

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
