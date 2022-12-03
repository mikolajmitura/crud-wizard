package pl.jalokim.crudwizard.genericapp.compiler;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CompiledCodeMetadataDto {

    Long id;
    String fullPath;
    String fullClassName;
    String simpleClassName;
    /**
     * used as classLoaderName as well
     */
    String sessionGenerationTimestamp;
    String generatedCodeHash;
}
