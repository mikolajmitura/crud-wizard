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
            tempFilesCleanerService.cleanTempDir(new TempDirCleanEvent("not passed validation"));
        }
    }
}
