package pl.jalokim.crudwizard.genericapp.afterrun;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import pl.jalokim.crudwizard.genericapp.compiler.ClassLoaderService;
import pl.jalokim.crudwizard.genericapp.metamodel.context.MetaModelContextService;

@Service
@RequiredArgsConstructor
public class AfterRunService implements ApplicationRunner {

    private final ClassLoaderService classLoaderService;
    private final MetaModelContextService metaModelContextService;

    @Override
    public void run(ApplicationArguments args) {
        classLoaderService.createClassLoaders();
        metaModelContextService.reloadAll();
    }
}
