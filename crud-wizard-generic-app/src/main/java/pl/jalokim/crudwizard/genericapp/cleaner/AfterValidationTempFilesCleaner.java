package pl.jalokim.crudwizard.genericapp.cleaner;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.jalokim.crudwizard.core.validation.javax.groups.ValidationResult;

@Component
@RequiredArgsConstructor
public class AfterValidationTempFilesCleaner {

    private final TempFilesCleanerService tempFilesCleanerService;

    public void cleanWhenValidationNotPassed(ValidationResult validationResult) {
        if (validationResult.hasErrors()) {
            // TODO after not passed validation should not clean classes for investigate why some class is not compile
            tempFilesCleanerService.cleanTempDir(new TempDirCleanEvent("not passed validation"));
        }
    }
}
