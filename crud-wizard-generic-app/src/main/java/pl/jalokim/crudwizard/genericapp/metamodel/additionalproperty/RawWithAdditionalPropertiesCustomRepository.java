package pl.jalokim.crudwizard.genericapp.metamodel.additionalproperty;

import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.reflection.InvokableReflectionUtils.getValueOfField;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RawWithAdditionalPropertiesCustomRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public WithAdditionalPropertiesEntity persist(WithAdditionalPropertiesEntity withAdditionalPropertiesEntity) {
        List<AdditionalPropertyEntity> additionalProperties = getValueOfField(withAdditionalPropertiesEntity, "additionalProperties");
        elements(additionalProperties).forEach(additionalProperty -> {
                additionalProperty.setOwnerClass(withAdditionalPropertiesEntity.getClass().getSimpleName());
                additionalProperty.setOwnerId(withAdditionalPropertiesEntity.getId());

                // TODO remove old properties somehow. Now is only add new and update old,
                //  but old which ones are not in getAdditionalProperties() will stay in db as well
                if (additionalProperty.getId() == null) {
                    // TODO use hibernate id generator somehow from entity
                    Long nextId = jdbcTemplate.queryForObject("call next value for hibernate_sequence", Long.class);
                    additionalProperty.setId(nextId);
                }
                jdbcTemplate.update("INSERT INTO " + AdditionalPropertyEntity.TABLE_NAME
                        + " (id, owner_class, owner_id, name, value_real_class_name, raw_json) VALUES(?,?,?,?,?,?)",
                    additionalProperty.getId(), additionalProperty.getOwnerClass(), additionalProperty.getOwnerId(),
                    additionalProperty.getName(), additionalProperty.getValueRealClassName(), additionalProperty.getRawJson());
            }
        );

        return withAdditionalPropertiesEntity;
    }
}
