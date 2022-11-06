package pl.jalokim.crudwizard.genericapp.util;

import static pl.jalokim.crudwizard.genericapp.metamodel.context.TemporaryModelContextHolder.getSessionTimeStamp;
import static pl.jalokim.utils.file.FileUtils.writeToFile;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import lombok.Getter;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.jalokim.utils.collection.CollectionUtils;
import pl.jalokim.utils.collection.Elements;
import pl.jalokim.utils.string.StringUtils;

@Component
public class CodeCompiler {

    @Getter
    private final String compiledCodeRootPath;

    public CodeCompiler(@Value("${crudWizard.compiledCodeRootPath:compiledCode}") String compiledCodeRootPath) {
        this.compiledCodeRootPath = compiledCodeRootPath;
    }

    /**
     * @return return full path for generated code
     */
    public String compileCodeAndReturnPath(String className, String packageName, String sourceCode) {
        File sourceFile = new File(StringUtils.concatElements("/", compiledCodeRootPath, getSessionTimeStamp().toString(),
            packageName.replace(".", "/"), className + ".java"));
        boolean createdFolder = sourceFile.getParentFile().mkdirs();
        if (!createdFolder) {
            if (!Files.exists(sourceFile.getParentFile().toPath())) {
                throw new IllegalStateException("cannot create directory with path: " + sourceFile.getParentFile());
            }
        }
        writeToFile(sourceFile, sourceCode);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        ByteArrayOutputStream errorOutPutStream = new ByteArrayOutputStream();
        int compileResult = compiler.run(null, null, errorOutPutStream, sourceFile.getPath());
        if (compileResult != 0) {
            String errorsContent = errorOutPutStream.toString(StandardCharsets.UTF_8);
            validateResults(errorsContent);
        }
        return sourceFile.toString().replaceAll("\\.java", ".class");
    }

    private void validateResults(String errorsContent) {
        List<String> errorLines = Elements.bySplitText(errorsContent, System.lineSeparator()).asList();
        String lastOrNull = CollectionUtils.getLastOrNull(errorLines);

        if (lastOrNull != null && lastOrNull.contains("error")) {
            List<String> lines = new ArrayList<>();
            for (String line : errorLines) {
                if (line.startsWith("Note: ")) {
                    break;
                }
                lines.add(line);
            }
            throw new IllegalStateException("compilation problems: " + System.lineSeparator() +
                Elements.elements(lines).concatWithNewLines());
        }
    }
}
