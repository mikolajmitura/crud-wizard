package pl.jalokim.crudwizard.genericapp.mapper.instance.objects;

import lombok.Data;

@Data
public class OtherPersonEntity {

    private Long id;
    private String name;
    private String surname;
    private Long externalId;
    private Long parentId;
    private String updatedBy;
    private String fromSpringBean;
    private PersonTypeEnum personType;
}
