package pl.jalokim.crudwizard.genericapp.compiler;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CompiledCodeRootPathProvider {

    @Getter
    private final String compiledCodeRootPath;

    public CompiledCodeRootPathProvider(@Value("${crudWizard.compiledCodeRootPath:compiledCode}") String compiledCodeRootPath) {
        this.compiledCodeRootPath = compiledCodeRootPath;
    }
}
