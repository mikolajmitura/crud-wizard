package pl.jalokim.crudwizard.genericapp.metamodel.classmodel.testsample;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class BeanWithJsonProperties {

    @JsonProperty("Field_1")
    private String field1;
    private String fieldNumber2;
    private String fieldNumber3;

    @JsonProperty("$Other_field")
    public void setOtherField(String field) {

    }

    @JsonProperty("$fieldNumber3_s")
    public void setFieldNumber3(String fieldNumber3) {
        this.fieldNumber3 = fieldNumber3;
    }

    @JsonProperty("$fieldNumber3_g")
    public String getFieldNumber3() {
        return fieldNumber3;
    }

    @JsonProperty("$root_id")
    public Long getId() {
        return 1232L;
    }

    public BeanWithJsonProperties(String field1,
        @JsonProperty("$fieldNumber2") String fieldNumber2,
        String fieldNumber3) {

        this.field1 = field1;
        this.fieldNumber2 = fieldNumber2;
        this.fieldNumber3 = fieldNumber3;
    }
}
