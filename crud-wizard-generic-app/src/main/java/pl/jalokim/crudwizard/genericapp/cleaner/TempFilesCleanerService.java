package pl.jalokim.crudwizard.genericapp.cleaner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pl.jalokim.crudwizard.core.utils.annotations.MetamodelService;
import pl.jalokim.crudwizard.genericapp.compiler.CompiledCodeRootPathProvider;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperMetaModelEntityRepository;
import pl.jalokim.crudwizard.genericapp.metamodel.mapper.MapperType;
import pl.jalokim.utils.collection.CollectionUtils;
import pl.jalokim.utils.collection.Elements;
import pl.jalokim.utils.file.FileUtils;

@MetamodelService
@RequiredArgsConstructor
@Slf4j
public class TempFilesCleanerService {

    private final MapperMetaModelEntityRepository mapperMetaModelEntityRepository;
    private final CompiledCodeRootPathProvider compiledCodeRootPathProvider;

    @SneakyThrows
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void cleanTempDir(TempDirCleanEvent tempDirCleanEvent) {
        log.info("start cleaning temp files source: " + tempDirCleanEvent.getSource());
        Set<String> allFoundFullPaths = new HashSet<>();
        mapperMetaModelEntityRepository.findAllByMapperType(MapperType.GENERATED)
            .forEach(mapperMetaModelEntity -> {
                var mapperGenerateConfiguration = mapperMetaModelEntity.getMapperGenerateConfiguration();
                var mapperCompiledCodeMetadata = mapperGenerateConfiguration.getMapperCompiledCodeMetadata();
                allFoundFullPaths.add(mapperCompiledCodeMetadata.getFullPath());
                allFoundFullPaths.add(mapperCompiledCodeMetadata.getFullPath().replace(".class", ".java"));
                allFoundFullPaths.add(mapperCompiledCodeMetadata.getFullPath().replace(".class", "$1.class"));
            });

        if (!Files.exists(Path.of(compiledCodeRootPathProvider.getCompiledCodeRootPath()))) {
            return;
        }
        log.debug("found all paths: \n{}", Elements.elements(allFoundFullPaths).concatWithNewLines());

        int deletedFiles = 0;
        List<Path> foundFiles = FileUtils.listOfFilesRecursively(compiledCodeRootPathProvider.getCompiledCodeRootPath(), Files::isRegularFile);
        for (Path currentPath : foundFiles) {
            if (!allFoundFullPaths.contains(currentPath.toString())) {
                log.debug("delete file: {}", currentPath);
                Files.delete(currentPath);
                deletedFiles++;
            }
        }

        List<Path> foundFolders = FileUtils.listOfFilesRecursively(compiledCodeRootPathProvider.getCompiledCodeRootPath(), Files::isDirectory);

        for (Path currentPath : foundFolders) {
            if (isNotRootDir(currentPath) && Files.exists(currentPath)) {
                List<Path> filesInFolder = FileUtils.listOfFilesRecursively(currentPath, Files::isRegularFile);
                if (CollectionUtils.isEmpty(filesInFolder)) {
                    log.info("delete directory: {}", currentPath);
                    FileUtils.deleteFileOrDirectory(currentPath);
                }
            }
        }

        log.info("deleted temp files: " + deletedFiles);
    }

    private boolean isNotRootDir(Path currentPath) {
        return !currentPath.toString().equals(Path.of(compiledCodeRootPathProvider.getCompiledCodeRootPath()).toString());
    }
}
