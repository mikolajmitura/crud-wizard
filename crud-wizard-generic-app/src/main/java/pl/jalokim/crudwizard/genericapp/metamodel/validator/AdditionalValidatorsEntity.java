package pl.jalokim.crudwizard.genericapp.metamodel.validator;

import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
@Table(name = "additional_validators")
public class AdditionalValidatorsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String fullPropertyPath;

    @ManyToMany
    @JoinTable(
        name = "additional_payload_validators",
        joinColumns = { @JoinColumn(name = "endpoint_path_id") },
        inverseJoinColumns = { @JoinColumn(name = "validator_meta_model_id") }
    )
    private List<ValidatorMetaModelEntity> validators;
}
