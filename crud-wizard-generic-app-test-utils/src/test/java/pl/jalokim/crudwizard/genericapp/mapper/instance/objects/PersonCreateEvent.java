package pl.jalokim.crudwizard.genericapp.mapper.instance.objects;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PersonCreateEvent {

    Long dbId;

    /*
    event uuid
     */
    String id;
    String fullName;
}
