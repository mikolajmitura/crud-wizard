package pl.jalokim.crudwizard.genericapp.metamodel.mapper;

import static pl.jalokim.crudwizard.genericapp.metamodel.context.TemporaryModelContextHolder.getSessionTimeStamp;
import static pl.jalokim.utils.file.FileUtils.deleteFileOrDirectory;

import java.nio.file.Files;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.genericapp.util.CodeCompiler;

@Component
@RequiredArgsConstructor
public class AfterValidationCleanTempFiles {

    private final CodeCompiler codeCompiler;

    public void cleanTempDir() {
        String pathToDelete = codeCompiler.getCompiledCodeRootPath() + "/" + getSessionTimeStamp();
        if (Files.exists(Path.of(pathToDelete))) {
            deleteFileOrDirectory(pathToDelete);
        }
    }
}
