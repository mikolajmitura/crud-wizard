package pl.jalokim.crudwizard.genericapp.metamodel.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AfterValidationCleanTempFiles {

    // TODO #1 create job which will be fired after validations,
    //  after reload context which will be clean not used classes and source codes
    public void cleanTempDir() {
//        String pathToDelete = codeCompiler.getCompiledCodeRootPath() + "/" + getSessionTimeStamp();
//        if (Files.exists(Path.of(pathToDelete))) {
//            deleteFileOrDirectory(pathToDelete);
//        }
    }
}
