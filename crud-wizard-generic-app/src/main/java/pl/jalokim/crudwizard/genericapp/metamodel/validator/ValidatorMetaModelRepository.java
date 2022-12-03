package pl.jalokim.crudwizard.genericapp.metamodel.validator;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ValidatorMetaModelRepository extends JpaRepository<ValidatorMetaModelEntity, Long> {

    @Query("from ValidatorMetaModelEntity v " +
        "where v.parametrized = false " +
        "and v.className = :className ")
    Optional<ValidatorMetaModelEntity> findByClassName(String className);

    @Query("from ValidatorMetaModelEntity v " +
        "where v.parametrized = false " +
        "and v.validatorName = :validatorName")
    Optional<ValidatorMetaModelEntity> findByValidatorName(String validatorName);
}
