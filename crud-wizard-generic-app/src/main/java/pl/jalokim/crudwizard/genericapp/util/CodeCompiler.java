package pl.jalokim.crudwizard.genericapp.util;

import static pl.jalokim.utils.file.FileUtils.writeToFile;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.springframework.stereotype.Component;
import pl.jalokim.utils.collection.CollectionUtils;
import pl.jalokim.utils.collection.Elements;

@Component
public class CodeCompiler {

    public void compileCode(String className, String packageName, String sourceCode) {
        // TODO #1 fix place for compiled files etc
        File sourceFile = new File("target/generatedMappers/" + packageName.replace(".", "/") + "/" + className + ".java");
        sourceFile.getParentFile().mkdirs();
        writeToFile(sourceFile, sourceCode);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        ByteArrayOutputStream errorOutPutStream = new ByteArrayOutputStream();
        compiler.run(null, null, errorOutPutStream, sourceFile.getPath());
        String errorsContent = errorOutPutStream.toString(StandardCharsets.UTF_8);
        validateResults(errorsContent);

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
