package pl.jalokim.crudwizard.genericapp.mapper.generete.config;

import lombok.Builder;
import lombok.Data;
import pl.jalokim.crudwizard.core.metamodels.ClassMetaModel;

@Data
@Builder(toBuilder = true)
public class MapperConfiguration {

    /**
     * Mapper name or method name
     */
    private String name;

    private ClassMetaModel sourceMetaModel;
    private ClassMetaModel targetMetaModel;

    /**
     * disable auto mapping, by default is enabled.
     * When is disabled then finding of conversion between types then no attempt will be made.
     */
    @Builder.Default
    // TODO #1 implements with usage that flag in MapperCodeGenerator
    private boolean enableAutoMapping = true;

    /**
     * by default disabled, should inform when have problem with some field, when cannot find conversion strategy for given field types.
     */
    @Builder.Default
    private boolean ignoreMappingProblems = false;

    @Builder.Default
    private PropertiesOverriddenMapping propertyOverriddenMapping = PropertiesOverriddenMapping.builder().build();

    @Builder.Default
    private EnumEntriesMapping enumEntriesMapping = EnumEntriesMapping.builder().build();

    public boolean isForMappingEnums() {
        return sourceMetaModel.isEnumTypeOrJavaEnum() && targetMetaModel.isEnumTypeOrJavaEnum();
    }
}
