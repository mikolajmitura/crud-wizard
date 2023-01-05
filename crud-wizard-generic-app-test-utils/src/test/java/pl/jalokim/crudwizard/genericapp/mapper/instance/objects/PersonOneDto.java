package pl.jalokim.crudwizard.genericapp.mapper.instance.objects;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PersonOneDto {

    Long dbId;
    String name;
    String surname;
    Long externalId;
    Long parentId;
    String updatedBy;
    String eventUuid;
}
