package pl.jalokim.crudwizard.genericapp.metamodel.classmodel;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
@Builder
public class EnumMetaModelDto {

    @NotEmpty
    private List<@Valid EnumEntryMetaModelDto> enums;
}
